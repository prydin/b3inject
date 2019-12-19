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

import net.virtualviking.b3inject.Logger;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ContextAgentIntercept extends ServiceFilterAgentIntercept {

    public static final String TRACING_FILTER_NAME = "tracingFilter";
    public static final String[] patterns = {"/*"};

    public static Object getAddFilterMethod(final ServletContext context) throws IllegalAccessException, InvocationTargetException, ServletException {
        if (ServiceFilterAgentIntercept.isContextRegistered(context)) {
            Logger.debug("Filter is already registered for " + context.getServletContextName());
            return null;
        }

        final Method addFilterMethod = getFilterMethod(context);
        if (addFilterMethod == null) {

                Logger.debug("Add filter method is missing for " + context.getServletContextName());
            return null;
        }

        final Filter tracingFilter = getFilter(context);
        return addFilterMethod.invoke(context, TRACING_FILTER_NAME, tracingFilter);
    }

    public static boolean invoke(final Object[] returned, final Object obj, final Method method, final Object ... args) {
        if (method == null)
            return false;

        try {
            returned[0] = method.invoke(obj, args);
            return true;
        }
        catch (final IllegalAccessException | InvocationTargetException e) {
            return false;
        }
    }

    public static Method getFilterMethod(final ServletContext context) {
        return getMethod(context.getClass(), "addFilter", String.class, Filter.class);
    }
}
