package org.jaggeryjs.apps;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "JaggeryAsyncServlet", urlPatterns = "/*", asyncSupported = true)
public class JaggeryAsyncServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final String NAME = "JaggeryAsyncServlet";

    protected void service(HttpServletRequest request,
                           HttpServletResponse response) throws ServletException, IOException {
        JaggeryAppConfigs appConfigs = JaggeryAppConfigs.getInstance(request.getServletContext());
        request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        AsyncContext asyncCtx = request.startAsync();
        asyncCtx.addListener(new JaggeryAsyncListener());
        asyncCtx.setTimeout(appConfigs.getServletTimeout());
        appConfigs.getServletExecutor().execute(new JaggeryAsyncRequestProcessor(asyncCtx));
    }
}
