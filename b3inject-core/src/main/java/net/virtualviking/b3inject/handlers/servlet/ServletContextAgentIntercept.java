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

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.util.EnumSet;


public class ServletContextAgentIntercept extends ContextAgentIntercept {
    public static boolean addFilter(final Object thiz) {
        if (!(thiz instanceof ServletContext)) {
            Logger.debug("Receiver does not inherit ServletContext");
            return false;
        }

        final ServletContext context = (ServletContext)thiz;
        try {
            final FilterRegistration.Dynamic registration = (FilterRegistration.Dynamic)getAddFilterMethod(context);
            if (registration == null)
                return false;

            registration.setAsyncSupported(true);
            registration.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, patterns);
            return true;
        }
        catch (final Exception e) {
            Logger.debug("Error adding servlet filter: " + e.toString());
            return false;
        }
    }
}

