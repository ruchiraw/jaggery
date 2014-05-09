package org.jaggeryjs.apps;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.core.JaggeryEngine;
import org.jaggeryjs.core.JaggeryException;
import org.jaggeryjs.core.JaggeryReader;
import org.jaggeryjs.core.JaggeryScript;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class JaggeryAsyncRequestProcessor implements Runnable {

    private static final Log log = LogFactory.getLog(JaggeryAsyncRequestProcessor.class);

    public static final String CONTEXT_KEY = "context";

    public static final String REQUEST_KEY = "request";

    public static final String RESPONSE_KEY = "response";

    private static final ThreadLocal<JaggeryEngine> threadLocalEngine = new ThreadLocal<JaggeryEngine>();

    private AsyncContext asyncContext;
    private int secs;

    public JaggeryAsyncRequestProcessor(AsyncContext asyncCtx, int secs) {
        this.asyncContext = asyncCtx;
        this.secs = secs;
    }

    @Override
    public void run() {
        System.out.println("Async Supported? "
                + asyncContext.getRequest().isAsyncSupported());
        longProcessing(secs);
        try {
            PrintWriter out = asyncContext.getResponse().getWriter();
            execute(asyncContext);
            out.write("Processing done for " + secs + " milliseconds!!");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JaggeryException e) {
            e.printStackTrace();
        }
        //complete the processing
        asyncContext.complete();
    }

    private void longProcessing(int secs) {
        // wait for given time before finishing
        try {
            Thread.sleep(secs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void execute(AsyncContext asyncContext) throws JaggeryException {
        ServletRequest request = asyncContext.getRequest();
        ServletResponse response = asyncContext.getResponse();

        JaggeryEngine engine = threadLocalEngine.get();
        if (engine == null) {
            final ServletContext servletContext = request.getServletContext();
            final String contextPath = servletContext.getContextPath();
            Map<String, Object> globals = new HashMap<String, Object>();
            globals.put(CONTEXT_KEY, servletContext);
            engine = new JaggeryEngine(
                    globals,
                    getReader(servletContext),
                    new JaggeryReader() {
                        @Override
                        public JaggeryScript getScript(String scriptId) throws IOException {
                            InputStream in = servletContext.getResourceAsStream(scriptId);
                            if (in == null) {
                                throw new IOException("script " + scriptId + " cannot be found at " +
                                        servletContext.getContextPath());
                            }
                            return new JaggeryScript("apps:/" + contextPath + scriptId, new InputStreamReader(in));
                        }
                    }
            );
            System.out.println("======================creating new engine====================");
            threadLocalEngine.set(engine);
        }
        Bindings options = new SimpleBindings();
        options.put(REQUEST_KEY, request);
        options.put(RESPONSE_KEY, response);
        engine.exec(options);
    }

    private JaggeryScript getReader(ServletContext servletContext) throws JaggeryException {
        String uri = servletContext.getInitParameter(JaggeryConstants.JAGGERY_INITIALIZER);
        if (uri == null) {
            throw new JaggeryException("cannot find " + JaggeryConstants.JAGGERY_INITIALIZER +
                    " property. Please define it via servlet init parameters");
        }
        if (uri.startsWith("file://")) {
            try {
                return new JaggeryScript(uri, new FileReader(FileUtils.toFile(new URL(uri))));
            } catch (MalformedURLException e) {
                throw new JaggeryException("malformed file url " + uri + " for " + JaggeryConstants.JAGGERY_INITIALIZER, e);
            } catch (FileNotFoundException e) {
                throw new JaggeryException(JaggeryConstants.JAGGERY_INITIALIZER + " file cannot be found at " + uri, e);
            }
        }
        if (uri.startsWith("server://")) {
            try {
                return new JaggeryScript(uri, new FileReader(FilenameUtils.normalizeNoEndSeparator(uri.substring(9))));
            } catch (FileNotFoundException e) {
                throw new JaggeryException(JaggeryConstants.JAGGERY_INITIALIZER + " file cannot be found at " + uri + " relative" +
                        " to the server url : " + new File("").getAbsolutePath(), e);
            }
        }
        if (uri.startsWith("app://")) {
            InputStream in = servletContext.getResourceAsStream(uri.substring(6));
            if (in == null) {
                throw new JaggeryException(JaggeryConstants.JAGGERY_INITIALIZER + " file cannot be found at " + uri + " relative" +
                        " to the app " + servletContext.getContextPath());
            }
            return new JaggeryScript(uri, new InputStreamReader(in));
        } else {
            throw new JaggeryException("unsupported file url format " + uri + " for " + JaggeryConstants.JAGGERY_INITIALIZER);
        }
    }
}
