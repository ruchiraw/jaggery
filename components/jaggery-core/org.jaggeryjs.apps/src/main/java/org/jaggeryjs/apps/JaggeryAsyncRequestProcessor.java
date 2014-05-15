package org.jaggeryjs.apps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.ObjectPool;
import org.jaggeryjs.core.JaggeryEngine;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class JaggeryAsyncRequestProcessor implements Runnable {

    private static final Log log = LogFactory.getLog(JaggeryAsyncRequestProcessor.class);

    private AsyncContext asyncContext;

    public JaggeryAsyncRequestProcessor(AsyncContext asyncCtx) {
        this.asyncContext = asyncCtx;
    }

    @Override
    public void run() {
        ServletRequest request = asyncContext.getRequest();
        ServletResponse response = asyncContext.getResponse();
        JaggeryAppConfigs appConfigs = JaggeryAppConfigs.getInstance(request.getServletContext());
        ObjectPool<JaggeryEngine> pool = appConfigs.getEnginePool();
        JaggeryEngine engine = null;
        try {
            engine = pool.borrowObject();
            Bindings options = new SimpleBindings();
            options.put(JaggeryConstants.REQUEST_OBJECT, request);
            options.put(JaggeryConstants.RESPONSE_OBJECT, response);
            engine.exec(options);
        } catch (Exception e) {
            String error = "Error executing callback for the request for : " + appConfigs.getContextPath();
            log.error(error, e);
            e.printStackTrace();
        } finally {
            asyncContext.complete();
            try {
                if (engine != null) {
                    if (appConfigs.isDevelopment()) {
                        pool.invalidateObject(engine);
                    } else {
                        pool.returnObject(engine);
                    }
                }
            } catch (Exception e) {
                // ignored
            }
        }
    }
}
