package org.jaggeryjs.apps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.core.JaggeryException;

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
        try {
            JaggeryAppConfigs.initialize(servletContext);
        } catch (JaggeryException e) {
            throw new RuntimeException("Error initializing Jaggery App : " + servletContext.getContextPath(), e);
        }
        ServletRegistration.Dynamic registration = servletContext.addServlet(
                JaggeryAsyncServlet.NAME, JaggeryAsyncServlet.class);
        registration.setAsyncSupported(true);
        registration.addMapping("/*");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        JaggeryAppConfigs appConfigs = JaggeryAppConfigs.getInstance(servletContext);
        appConfigs.getServletExecutor().shutdown();
        appConfigs.getEnginePool().close();
    }

}
