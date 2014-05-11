package org.jaggeryjs.apps;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

@WebServlet(name = "JaggeryAsyncServlet", urlPatterns = "/*", asyncSupported = true)
public class JaggeryAsyncServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final String NAME = "JaggeryAsyncServlet";

    public void init() {

    }

    protected void service(HttpServletRequest request,
                           HttpServletResponse response) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        /*System.out.println("AsyncLongRunningServlet Start::Name="
                + Thread.currentThread().getName() + "::ID="
                + Thread.currentThread().getId());*/

        request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);

        String time = request.getParameter("time");
        int secs = Integer.valueOf(time);
        // max 10 seconds
        if (secs > 10000)
            secs = 10000;

        AsyncContext asyncCtx = request.startAsync();
        asyncCtx.addListener(new JaggeryAsyncListener());
        asyncCtx.setTimeout(9000);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) request
                .getServletContext().getAttribute(JaggeryConstants.SERVLET_EXECUTOR);

        executor.execute(new JaggeryAsyncRequestProcessor(asyncCtx, secs));
        long endTime = System.currentTimeMillis();
        /*System.out.println("AsyncLongRunningServlet End::Name="
                + Thread.currentThread().getName() + "::ID="
                + Thread.currentThread().getId() + "::Time Taken="
                + (endTime - startTime) + " ms.");*/
    }
}
