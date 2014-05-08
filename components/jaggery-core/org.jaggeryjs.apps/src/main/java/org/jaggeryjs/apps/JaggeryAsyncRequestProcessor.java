package org.jaggeryjs.apps;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.core.JaggeryEngine;
import org.jaggeryjs.core.JaggeryException;
import org.jaggeryjs.core.JaggeryReader;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;

public class JaggeryAsyncRequestProcessor implements Runnable {

    private static final Log log = LogFactory.getLog(JaggeryAsyncRequestProcessor.class);

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
            execute(asyncContext.getRequest().getServletContext());
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

    private void execute(final ServletContext servletContext) throws JaggeryException {
        System.out.println(new File("").getAbsolutePath());
        JaggeryEngine engine = new JaggeryEngine("apps",
                getReader(servletContext),
                new JaggeryReader() {
                    @Override
                    public Reader getReader(String scriptId) throws IOException {
                        InputStream in = servletContext.getResourceAsStream(scriptId);
                        if (in == null) {
                            throw new IOException("script " + scriptId + " cannot be found at " +
                                    servletContext.getContextPath());
                        }
                        return new InputStreamReader(in);
                    }
                }
        );
        Bindings bindings = new SimpleBindings();
        bindings.put("request", "req");
        bindings.put("response", "res");
        engine.exec(bindings);
    }

    private Reader getReader(ServletContext servletContext) throws JaggeryException {
        String uri = servletContext.getInitParameter(JaggeryConstants.JAGGERY_INITIALIZER);
        if (uri == null) {
            throw new JaggeryException("cannot find " + JaggeryConstants.JAGGERY_INITIALIZER +
                    " property. Please define it via servlet init parameters");
        }
        if (uri.startsWith("file://")) {
            try {
                return new FileReader(FileUtils.toFile(new URL(uri)));
            } catch (MalformedURLException e) {
                throw new JaggeryException("malformed file url " + uri + " for " + JaggeryConstants.JAGGERY_INITIALIZER, e);
            } catch (FileNotFoundException e) {
                throw new JaggeryException(JaggeryConstants.JAGGERY_INITIALIZER + " file cannot be found at " + uri, e);
            }
        }
        if (uri.startsWith("server://")) {
            try {
                return new FileReader(FilenameUtils.normalizeNoEndSeparator(uri.substring(9)));
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
            return new InputStreamReader(in);
        } else {
            throw new JaggeryException("unsupported file url format " + uri + " for " + JaggeryConstants.JAGGERY_INITIALIZER);
        }
    }
}
