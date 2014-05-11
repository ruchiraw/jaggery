package org.jaggeryjs.apps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jaggeryjs.core.JaggeryEngine;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@WebListener
public class JaggeryContextListener implements ServletContextListener {

    private static final Log log = LogFactory.getLog(JaggeryContextListener.class);

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        // create the servlet executor thread pool
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                getInteger(servletContext, JaggeryConstants.SERVLET_EXECUTOR_POOL_MIN, 200),
                getInteger(servletContext, JaggeryConstants.SERVLET_EXECUTOR_POOL_MAX, 400),
                getLong(servletContext, JaggeryConstants.SERVLET_EXECUTOR_KEEPALIVE, 10000L),
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(
                        getInteger(servletContext, JaggeryConstants.SERVLET_EXECUTOR_REQUEST_QUEUE, 10000))
        );
        servletContext.setAttribute(JaggeryConstants.SERVLET_EXECUTOR, executor);

        // create script engine thread pool
        GenericObjectPoolConfig enginePoolConfig = new GenericObjectPoolConfig();
        enginePoolConfig.setMinIdle(getInteger(servletContext, JaggeryConstants.ENGINE_POOL_MIN_IDLE, 100));
        enginePoolConfig.setMaxIdle(getInteger(servletContext, JaggeryConstants.ENGINE_POOL_MAX_IDLE, 200));
        enginePoolConfig.setMaxTotal(getInteger(servletContext, JaggeryConstants.ENGINE_POOL_MAX_ACTIVE, 500));
        enginePoolConfig.setMaxWaitMillis(getLong(servletContext, JaggeryConstants.ENGINE_POOL_MAX_WAIT, -1L));
        GenericObjectPool<JaggeryEngine> enginePool = new GenericObjectPool<JaggeryEngine>(
                new JaggeryEngineFactory(servletContext), enginePoolConfig);
        servletContext.setAttribute(JaggeryConstants.ENGINE_POOL, enginePool);

        ServletRegistration registration = servletContext.getServletRegistration(JaggeryAsyncServlet.NAME);
        if (registration == null) {
            registration = servletContext.addServlet(JaggeryAsyncServlet.NAME, JaggeryAsyncServlet.class);
        }
        registration.addMapping("/*");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) servletContext.getAttribute(JaggeryConstants.SERVLET_EXECUTOR);
        executor.shutdown();

        @SuppressWarnings("unchecked")
        GenericObjectPool<JaggeryEngine> enginePool = (GenericObjectPool<JaggeryEngine>) servletContext.getAttribute(
                JaggeryConstants.ENGINE_POOL);
        enginePool.close();
    }

    private int getInteger(ServletContext servletContext, String name, int val) {
        String param = servletContext.getInitParameter(name);
        return param != null ? Integer.parseInt(param) : val;
    }

    private long getLong(ServletContext servletContext, String name, long val) {
        String param = servletContext.getInitParameter(name);
        return param != null ? Long.parseLong(param) : val;
    }
}
