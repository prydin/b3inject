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
package net.virtualviking.b3inject.handlers;

import net.bytebuddy.agent.builder.AgentBuilder;
import static net.bytebuddy.matcher.ElementMatchers.*;
import net.bytebuddy.asm.Advice;
import net.virtualviking.b3inject.Matchers;

public class JettyIngressHandler {
    @Advice.OnMethodEnter
    public static void enter(final @Advice.AllArguments Object[] args) {
        new B3CaptureFilter();
        GenericIngressHandler.enter(args[1], "getHeader");
    }

    @Advice.OnMethodExit
    public static void exit() {
        GenericIngressHandler.exit();
    }

    public static AgentBuilder buildAgent(AgentBuilder b) {
        return b.type(named("org.eclipse.jetty.server.handler.HandlerWrapper"))
                .transform((builder, type, classLoader, module) ->
                        builder
                                .visit(Advice.to(JettyIngressHandler.class).on(new Matchers.WildcardMethodMatcher(
                                        "handle(java.lang.String,org.eclipse.jetty.server.Request,"+
                                        "javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)"))));
    }
}
