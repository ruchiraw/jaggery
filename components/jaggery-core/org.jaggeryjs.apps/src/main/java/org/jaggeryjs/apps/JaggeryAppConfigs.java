package org.jaggeryjs.apps;

import org.apache.commons.io.FileUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jaggeryjs.core.JaggeryEngine;
import org.jaggeryjs.core.JaggeryException;
import org.jaggeryjs.core.JaggeryFile;
import org.jaggeryjs.core.JaggeryReader;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JaggeryAppConfigs {

    private ThreadPoolExecutor servletExecutor;

    private ObjectPool<JaggeryEngine> enginePool;

    private boolean isDevelopment = false;

    private JaggeryFile initializer;

    private JaggeryReader reader;

    private long servletTimeout = 1000L;

    private String contextPath;

    private String jaggeryHome;

    private JaggeryAppConfigs(ServletContext servletContext) throws JaggeryException {
        // create the servlet executor thread pool
        this.servletExecutor = createServletExecutor(servletContext);
        // create script engine thread pool
        this.enginePool = createEnginePool(servletContext);
        this.isDevelopment = getBoolean(servletContext, JaggeryConstants.DEVELOPMENT_MODE, isDevelopment);
        this.jaggeryHome = getJaggeryHome(servletContext);
        this.initializer = getInitializer(servletContext);
        this.contextPath = servletContext.getContextPath();
        this.reader = getReader(servletContext, this.contextPath);
        this.servletTimeout = getLong(servletContext, JaggeryConstants.ASYNC_SERVLET_TIMEOUT, servletTimeout);
    }

    private String getJaggeryHome(ServletContext servletContext) throws JaggeryException {
        String home = servletContext.getInitParameter(JaggeryConstants.HOME);
        if (home == null) {
            throw new JaggeryException("Error reading jaggery.home property");
        }
        if (!home.startsWith("file://")) {
            throw new JaggeryException("Invalid value for jaggery.home property : " + home);
        }
        if (home.endsWith("/")) {
            home = home.substring(0, home.length() - 1);
        }
        try {
            return FileUtils.toFile(new URL(home)).getAbsolutePath();
        } catch (MalformedURLException e) {
            throw new JaggeryException(e.getMessage(), e);
        }
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

    public JaggeryFile getInitializer() {
        return initializer;
    }

    public long getServletTimeout() {
        return servletTimeout;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getJaggeryHome() {
        return jaggeryHome;
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

    private String getString(ServletContext servletContext, String name, String val) {
        String param = servletContext.getInitParameter(name);
        return param != null ? param : val;
    }

    private JaggeryFile getInitializer(ServletContext servletContext) throws JaggeryException {
        String uri = servletContext.getInitParameter(JaggeryConstants.INITIALIZER);
        if (uri == null) {
            throw new JaggeryException("Cannot find " + JaggeryConstants.INITIALIZER +
                    " property. Please define it via servlet init parameters");
        }
        if (uri.startsWith("server://")) {
            final String path = resolvePath(uri.substring(9));
            final File initializer = new File(path);
            return new JaggeryFile() {
                @Override
                public String getId() {
                    return path;
                }

                @Override
                public boolean isExists() {
                    return initializer.exists();
                }

                @Override
                public Reader getReader() throws JaggeryException {
                    try {
                        return new FileReader(initializer);
                    } catch (FileNotFoundException e) {
                        throw new JaggeryException(JaggeryConstants.INITIALIZER +
                                " file cannot be found at " + initializer.toURI().toString(), e);
                    }
                }
            };
        }
        JaggeryAppConfigs appConfigs = JaggeryAppConfigs.getInstance(servletContext);
        if (uri.startsWith("app://")) {
            String path = uri.substring(5);
            final String id = "apps:/" + appConfigs.getContextPath() + path;
            final InputStream in = servletContext.getResourceAsStream(path);
            return new JaggeryFile() {
                @Override
                public String getId() {
                    return id;
                }

                @Override
                public boolean isExists() {
                    return in != null;
                }

                @Override
                public Reader getReader() {
                    return new InputStreamReader(in);
                }
            };
        } else {
            throw new JaggeryException("Unsupported file url format " + uri + " for " + JaggeryConstants.INITIALIZER);
        }
    }

    private JaggeryReader getReader(final ServletContext servletContext, final String contextPath) {
        return new JaggeryReader() {
            @Override
            public JaggeryFile getFile(final String path) throws JaggeryException {
                final String id = "apps:/" + contextPath + path;
                final InputStream in = servletContext.getResourceAsStream(path);
                return new JaggeryFile() {
                    @Override
                    public String getId() {
                        return id;
                    }

                    @Override
                    public boolean isExists() {
                        return in != null;
                    }

                    @Override
                    public Reader getReader() {
                        return new InputStreamReader(in);
                    }
                };
            }
        };
    }

    private String resolvePath(String path) {
        return this.jaggeryHome + File.separator + path.replaceAll("/", File.separator);
    }
}
