/*
 *  Copyright 2019 Pontus Rydin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// NOTICE: This file is based on code from the Opentracing Java Special Agent. It is redistributed under the
// same license (Apache License 2.0). Some changes have been made to conform to the general B3Inject framework.

package net.virtualviking.b3inject.handlers.servlet;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ServiceFilterAgentIntercept {
    private static final Map<Object, ServletContext> filterOrServletToServletContext = new HashMap<>();
    private static final Map<ServletRequest,Boolean> servletRequestToState = new HashMap<>();
    private static final Map<ServletContext, Filter> servletContextToFilter = new HashMap<>();

    public static boolean isContextRegistered(final ServletContext context) {
        synchronized (servletContextToFilter) {
            return servletContextToFilter.containsKey(context);
        }
    }

    public static Filter getFilter(final ServletContext context) throws ServletException {
        synchronized (servletContextToFilter) {
            Filter filter = servletContextToFilter.get(context);
            if (filter != null)
                return filter;

            servletContextToFilter.put(context, filter = new B3CaptureFilter());
            return filter;
        }
    }

    public static Method getMethod(final Class<?> cls, final String name, final Class<?> ... parameterTypes) {
        try {
            final Method method = cls.getMethod(name, parameterTypes);
            return Modifier.isAbstract(method.getModifiers()) ? null : method;
        }
        catch (final NoClassDefFoundError | NoSuchMethodException e) {
            return null;
        }
    }
}
