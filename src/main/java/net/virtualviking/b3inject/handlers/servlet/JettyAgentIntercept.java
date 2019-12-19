package net.virtualviking.b3inject.handlers.servlet;

import net.virtualviking.b3inject.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;

public class JettyAgentIntercept extends ContextAgentIntercept {
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static boolean addFilter(final Object thiz) {
        ServletContext context = null;
        try {
            context = (ServletContext)thiz.getClass().getMethod("getServletContext").invoke(thiz);
            final Object registration = getAddFilterMethod(context);
            if (registration == null)
                return false;

            final Class<?> registrationClass = registration.getClass();
            final Method addMappingForUrlPatternsMethod = registrationClass.getMethod("addMappingForUrlPatterns", EnumSet.class, boolean.class, String[].class);
            final Method setAsyncSupportedMethod = registrationClass.getMethod("setAsyncSupported", boolean.class);
            setAsyncSupportedMethod.invoke(registration, Boolean.TRUE);

            EnumSet dispatcherTypes = null;
            try {
                final Class<Enum> dispatcherTypeClass = (Class<Enum>)Class.forName("javax.servlet.DispatcherType");
                dispatcherTypes = EnumSet.allOf(dispatcherTypeClass);
            }
            catch (final ClassNotFoundException e) {
                Logger.debug("Error loading class: " + e.toString());
            }

            addMappingForUrlPatternsMethod.invoke(registration, dispatcherTypes, true, patterns);
            return true;
        }
        catch (final IllegalAccessException | NoSuchMethodException | ServletException | InvocationTargetException e) {
            Logger.debug("Error while building Jetty filter: " + e.toString());
            return false;
        }
    }
}
