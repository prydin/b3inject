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
import net.bytebuddy.asm.Advice;
import net.virtualviking.b3inject.Constants;
import net.virtualviking.b3inject.Context;
import net.virtualviking.b3inject.Logger;
import net.virtualviking.b3inject.Matchers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class SpringEgressHandler {
    @Advice.OnMethodEnter
    public static void enter(final @Advice.Origin String origin, final @Advice.This Object self, final @Advice.AllArguments Object[] args) {
        Logger.debug("Entering instrumentation on " + origin);
        GenericEgressHandler.enter(args[0], "set");
    }

    @Advice.OnMethodExit
    public static void exit() {
        GenericEgressHandler.exit();
    }

    public static AgentBuilder buildAgent(AgentBuilder b) {
        return b.type(hasSuperType(named("org.springframework.http.client.AbstractClientHttpRequest")))
                .transform((builder, type, classLoader, module) ->
                        builder
                                .visit(Advice.to(SpringEgressHandler.class).on(new Matchers.WildcardMethodMatcher(
                                        "executeInternal(org.springframework.http.HttpHeaders)"))))
                .type(hasSuperType(named("org.springframework.web.reactive.function.client.WebClient$RequestHeadersSpec")))
                .transform((builder, type, classLoader, module) ->
                        builder
                                .visit(Advice.to(WebFluxHandler.class).on(isConstructor())));
    }

    public static class WebFluxHandler {
        public static void enter(Object rq) {
            Context context = Context.Factory.getContext();
            if(context == null) {
                return;
            }
            if(context.isEgressHandled()) {
                return;
            }
            context.setEgressHandled(true);
            try {
                Logger.debug("EGRESS: Passing B3 headers: " + Logger.mapToString(context.getB3Headers()));
                Method m = rq.getClass().getMethod("header", String.class, String[].class);
                m.setAccessible(true);
                for(String h : Constants.b3Headers) {
                    String value = context.getB3Headers().get(h);
                    if (value == null) {
                        continue;
                    }
                    m.invoke(rq, h, new String[] { value });
                }
            } catch (NoSuchMethodException|IllegalAccessException| InvocationTargetException e){
                e.printStackTrace();
            }
        }
        @Advice.OnMethodExit
        public static void exit(final @Advice.Origin String origin, @Advice.This Object self) {
            Logger.debug("Entering instrumentation on " + origin);
            WebFluxHandler.enter(self);
        }
    }
}
