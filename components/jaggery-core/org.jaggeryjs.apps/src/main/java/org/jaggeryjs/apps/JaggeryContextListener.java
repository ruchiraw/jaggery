package org.jaggeryjs.apps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

@WebListener
public class JaggeryContextListener implements ServletContextListener {

    private static final Log log = LogFactory.getLog(JaggeryContextListener.class);

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                return thread;
            }
        };
        // create the thread pool
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                getInteger(servletContext, JaggeryConstants.JAGGERY_POOL_MIN, 200),
                getInteger(servletContext, JaggeryConstants.JAGGERY_POOL_MAX, 400),
                getLong(servletContext, JaggeryConstants.JAGGERY_KEEPALIVE, 10000L),
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(
                        getInteger(servletContext, JaggeryConstants.JAGGERY_REQUEST_QUEUE, 10000)),
                factory
        );
        servletContext.setAttribute(JaggeryConstants.JAGGERY_EXECUTOR, executor);
        ServletRegistration registration = servletContext.getServletRegistration(JaggeryAsyncServlet.NAME);
        if (registration == null) {
            registration = servletContext.addServlet(JaggeryAsyncServlet.NAME, JaggeryAsyncServlet.class);
        }
        registration.addMapping("/*");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) servletContext.getAttribute(JaggeryConstants.JAGGERY_EXECUTOR);
        executor.shutdown();
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
