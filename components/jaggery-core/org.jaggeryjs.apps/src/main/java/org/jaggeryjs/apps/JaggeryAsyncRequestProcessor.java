package org.jaggeryjs.apps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.ObjectPool;
import org.jaggeryjs.core.JaggeryEngine;
import org.jaggeryjs.core.JaggeryException;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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
        /*System.out.println("Async Supported? "
                + asyncContext.getRequest().isAsyncSupported());*/
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
        ServletContext servletContext = request.getServletContext();
        @SuppressWarnings("unchecked")
        ObjectPool<JaggeryEngine> pool = (ObjectPool<JaggeryEngine>) servletContext.getAttribute(
                JaggeryConstants.ENGINE_POOL);
        JaggeryEngine engine = null;
        try {
            engine = pool.borrowObject();
            Bindings options = new SimpleBindings();
            options.put(JaggeryConstants.REQUEST_KEY, request);
            options.put(JaggeryConstants.RESPONSE_KEY, response);
            engine.exec(options);
        } catch (Exception e) {
            throw new JaggeryException("Error borrowing JaggeryEngine from engine pool", e);
        } finally {
            try {
                if (engine != null) {
                    pool.returnObject(engine);
                }
            } catch (Exception e) {
                // ignored
            }
        }
    }

}
