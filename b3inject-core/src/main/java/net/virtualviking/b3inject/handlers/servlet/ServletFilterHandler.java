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

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ServletFilterHandler {
    public static boolean filterAdded = false;

    public static AgentBuilder buildAgent(final AgentBuilder b) throws Exception {
        return b
                .type(named("org.eclipse.jetty.servlet.ServletContextHandler"))
                .transform((builder, typeDesc, loader, module) ->
                        builder.visit(Advice.to(JettyAdvice.class).on(isConstructor())))
                .type(not(isInterface()).and(hasSuperType(named("javax.servlet.ServletContext"))
                        // Jetty is handled separately due to the (otherwise) need for tracking state of the ServletContext
                        .and(not(nameStartsWith("org.eclipse.jetty")))
                        // Similarly, ApplicationContextFacade causes trouble and it's enough to instrument ApplicationContext
                        .and(not(named("org.apache.catalina.core.ApplicationContextFacade")))
                        // Otherwise we are breaking Tomcat 8.5+
                        .and(not(named("org.apache.catalina.core.StandardContext$NoPluggabilityServletContext")))))
                .transform((builder, typeDesc, loader, module)->
                        builder.visit(Advice.to(ServletContextAdvice.class).on(isConstructor())));
    }

    public static class JettyAdvice {
        @Advice.OnMethodExit
        public static void exit(final @Advice.Origin String origin, final @Advice.This Object thiz) {
            filterAdded = JettyAgentIntercept.addFilter(thiz);
        }
    }

    public static class ServletContextAdvice {
        @Advice.OnMethodExit
        public static void exit(final @Advice.Origin String origin, final @Advice.This Object thiz) {
            filterAdded = ServletContextAgentIntercept.addFilter(thiz);
        }
    }
}
