package api.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(urlPatterns = "/api/*")
public class LoggingFilterApi implements Filter {

    private boolean debugLog = true;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (debugLog) System.out.println("LoggingFilterApi.init()");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (debugLog) {
            String log = ((HttpServletRequest) servletRequest).getMethod() + " " + ((HttpServletRequest) servletRequest).getRequestURI();
            System.out.println("LoggingFilterApi.doFilter() START " + log);

            filterChain.doFilter(servletRequest, servletResponse);

            System.out.println("LoggingFilterApi.doFilter() FINISH " + log);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {
        if (debugLog) System.out.println("LoggingFilterApi.destroy()");
    }

}
