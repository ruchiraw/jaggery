package org.jaggeryjs.apps;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jaggeryjs.core.JaggeryEngine;
import org.jaggeryjs.core.JaggeryException;
import org.jaggeryjs.core.JaggeryScript;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JaggeryAppConfigs {

    private ThreadPoolExecutor servletExecutor;

    private ObjectPool<JaggeryEngine> enginePool;

    private boolean isDevelopment = false;

    private JaggeryScript initializer;

    private long servletTimeout = 1000L;

    private String contextPath;

    private JaggeryAppConfigs(ServletContext servletContext) throws JaggeryException {
        // create the servlet executor thread pool
        this.servletExecutor = createServletExecutor(servletContext);
        // create script engine thread pool
        this.enginePool = createEnginePool(servletContext);
        this.isDevelopment = getBoolean(servletContext, JaggeryConstants.DEVELOPMENT_MODE, isDevelopment);
        this.initializer = getInitializer(servletContext);
        this.servletTimeout = getLong(servletContext, JaggeryConstants.ASYNC_SERVLET_TIMEOUT, servletTimeout);
        this.contextPath = servletContext.getContextPath();
    }

    public static JaggeryAppConfigs getInstance(ServletContext servletContext) {
        return (JaggeryAppConfigs) servletContext.getAttribute(JaggeryConstants.APP_CONFIGS);
    }

    public static void initialize(ServletContext servletContext) throws JaggeryException {
        servletContext.setAttribute(JaggeryConstants.APP_CONFIGS, new JaggeryAppConfigs(servletContext));
    }

    public ThreadPoolExecutor getServletExecutor() {
        return servletExecutor;
    }

    public ObjectPool<JaggeryEngine> getEnginePool() {
        return enginePool;
    }

    public boolean isDevelopment() {
        return isDevelopment;
    }

    public JaggeryScript getInitializer() {
        return initializer;
    }

    public long getServletTimeout() {
        return servletTimeout;
    }

    public String getContextPath() {
        return contextPath;
    }

    private GenericObjectPool<JaggeryEngine> createEnginePool(ServletContext servletContext) {
        GenericObjectPoolConfig enginePoolConfig = new GenericObjectPoolConfig();
        enginePoolConfig.setMinIdle(getInteger(servletContext, JaggeryConstants.ENGINE_POOL_MIN_IDLE, 100));
        enginePoolConfig.setMaxIdle(getInteger(servletContext, JaggeryConstants.ENGINE_POOL_MAX_IDLE, 200));
        enginePoolConfig.setMaxTotal(getInteger(servletContext, JaggeryConstants.ENGINE_POOL_MAX_ACTIVE, 500));
        enginePoolConfig.setMaxWaitMillis(getLong(servletContext, JaggeryConstants.ENGINE_POOL_MAX_WAIT, -1L));
        return new GenericObjectPool<JaggeryEngine>(
                new JaggeryEngineFactory(servletContext), enginePoolConfig);
    }

    private ThreadPoolExecutor createServletExecutor(ServletContext servletContext) {
        return new ThreadPoolExecutor(
                getInteger(servletContext, JaggeryConstants.EXECUTOR_POOL_MIN, 200),
                getInteger(servletContext, JaggeryConstants.EXECUTOR_POOL_MAX, 400),
                getLong(servletContext, JaggeryConstants.EXECUTOR_KEEPALIVE, 10000L),
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(
                        getInteger(servletContext, JaggeryConstants.EXECUTOR_REQUEST_QUEUE, 10000))
        );
    }

    private int getInteger(ServletContext servletContext, String name, int val) {
        String param = servletContext.getInitParameter(name);
        return param != null ? Integer.parseInt(param) : val;
    }

    private long getLong(ServletContext servletContext, String name, long val) {
        String param = servletContext.getInitParameter(name);
        return param != null ? Long.parseLong(param) : val;
    }

    private boolean getBoolean(ServletContext servletContext, String name, boolean val) {
        String param = servletContext.getInitParameter(name);
        return param != null ? Boolean.parseBoolean(param) : val;
    }

    private JaggeryScript getInitializer(ServletContext servletContext) throws JaggeryException {
        String uri = servletContext.getInitParameter(JaggeryConstants.INITIALIZER);
        if (uri == null) {
            throw new JaggeryException("cannot find " + JaggeryConstants.INITIALIZER +
                    " property. Please define it via servlet init parameters");
        }
        if (uri.startsWith("file://")) {
            try {
                return new JaggeryScript(uri, new FileReader(FileUtils.toFile(new URL(uri))));
            } catch (MalformedURLException e) {
                throw new JaggeryException("malformed file url " + uri + " for " + JaggeryConstants.INITIALIZER, e);
            } catch (FileNotFoundException e) {
                throw new JaggeryException(JaggeryConstants.INITIALIZER + " file cannot be found at " + uri, e);
            }
        }
        if (uri.startsWith("server://")) {
            try {
                return new JaggeryScript(uri, new FileReader(FilenameUtils.normalizeNoEndSeparator(uri.substring(9))));
            } catch (FileNotFoundException e) {
                throw new JaggeryException(JaggeryConstants.INITIALIZER + " file cannot be found at " + uri + " relative" +
                        " to the server url : " + new File("").getAbsolutePath(), e);
            }
        }
        if (uri.startsWith("app://")) {
            InputStream in = servletContext.getResourceAsStream(uri.substring(6));
            if (in == null) {
                throw new JaggeryException(JaggeryConstants.INITIALIZER + " file cannot be found at " + uri + " relative" +
                        " to the app " + servletContext.getContextPath());
            }
            return new JaggeryScript(uri, new InputStreamReader(in));
        } else {
            throw new JaggeryException("unsupported file url format " + uri + " for " + JaggeryConstants.INITIALIZER);
        }
    }
}
