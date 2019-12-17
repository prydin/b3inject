package net.virtualviking.b3inject.handlers;

import net.virtualviking.b3inject.Constants;
import net.virtualviking.b3inject.Context;
import net.virtualviking.b3inject.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;

public class B3CaptureFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Context context = Context.Factory.getContext();
        if(context == null) {
            return;
        }
        if(!(request instanceof HttpServletRequest)) {
            return;
        }
        Map<String, String> headers = context.getB3Headers();
        HttpServletRequest hr = (HttpServletRequest) request;
        for(String k : Constants.b3Headers) {
            String v = hr.getHeader(k);
            if(v != null) {
                headers.put(k, v);
            }
        }
        Logger.debug("INGRESS: Captured B3 headers: " + Logger.mapToString(headers));
    }

    @Override
    public void destroy() {
    }

    public static void addTo(Object o) {
        ServletContext sc = (ServletContext) o;
        sc.addFilter("B3CaptureFilter", new B3CaptureFilter())
                .addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), false, "/*");
    }
}
