package org.jaggeryjs.apps;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.jaggeryjs.core.JaggeryEngine;
import org.jaggeryjs.core.JaggeryException;
import org.jaggeryjs.core.JaggeryReader;
import org.jaggeryjs.core.JaggeryScript;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class JaggeryEngineFactory extends BasePooledObjectFactory<JaggeryEngine> {

    private ServletContext servletContext;

    private String contextPath;

    public JaggeryEngineFactory(ServletContext servletContext) {
        this.servletContext = servletContext;
        this.contextPath = servletContext.getContextPath();
    }

    @Override
    public JaggeryEngine create() throws JaggeryException {
        Map<String, Object> globals = new HashMap<String, Object>();
        JaggeryReader reader = new JaggeryReader() {
            @Override
            public JaggeryScript getScript(String scriptId) throws IOException {
                InputStream in = servletContext.getResourceAsStream(scriptId);
                if (in == null) {
                    throw new IOException("script " + scriptId + " cannot be found at " +
                            servletContext.getContextPath());
                }
                return new JaggeryScript("apps:/" + contextPath + scriptId, new InputStreamReader(in));
            }
        };
        globals.put("reader", reader);
        globals.put(JaggeryConstants.CONTEXT_KEY, servletContext);
        return new JaggeryEngine(globals, getReader(servletContext));
    }

    @Override
    public PooledObject<JaggeryEngine> wrap(JaggeryEngine engine) {
        return new DefaultPooledObject<JaggeryEngine>(engine);
    }

    @Override
    public void passivateObject(PooledObject<JaggeryEngine> pooledObject) {
        //pooledObject.getObject();
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