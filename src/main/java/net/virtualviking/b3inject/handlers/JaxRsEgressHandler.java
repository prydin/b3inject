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

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;


public class JaxRsEgressHandler {

    public static class B3HeadersRequestFilter implements ClientRequestFilter {
        @Override
        public void filter(ClientRequestContext clientRequest) throws IOException {
            Context context = Context.Factory.getContext();
            if(context == null) {
                return;
            }
            if(context.isEgressHandled()) {
                return;
            }
            Logger.debug("EGRESS: Passing B3 headers: " + Logger.mapToString(context.getB3Headers()));
            for(String k : Constants.b3Headers) {
                String v = context.getB3Headers().get(k);
                if(v != null) {
                    clientRequest.getHeaders().add(k, v);
                }
            }
            context.setEgressHandled(true);
        }
    }

    public static AgentBuilder buildAgent(final AgentBuilder b) {
        return b.type(hasSuperType(hasSuperType(named("javax.ws.rs.client.ClientBuilder"))))
                .transform((builder, type, classLoader, module) ->
                        builder.visit(Advice.to(JaxRsEgressHandler.class)
                                .on(new Matchers.WildcardMethodMatcher("build(*)"))))
                .type(hasSuperType(named("javax.ws.rs.client.Invocation$Builder")))
                .transform((builder, type, classLoader, loader) ->
                        builder.visit(Advice.to(JaxRsEgressHandler.AsyncHandler.class).on(named("async"))));
    }

    @Advice.OnMethodEnter
    public static void enter(final @Advice.Origin String origin, final @Advice.This Object self) {
        Logger.debug("Entering instrumentation on " + origin);
        final ClientBuilder builder = (ClientBuilder)self;
        builder.register(B3HeadersRequestFilter.class);
    }

    public static class AsyncHandler {
        // This handler is needed since the filter defined above will be executed in a separate thread
        // for asynchronous calls.
        @Advice.OnMethodEnter
        public static void enter(final @Advice.Origin String origin, final @Advice.This Object self) {
            Logger.debug("Entering instrumentation on " + origin);
            GenericEgressHandler.enter(self, "header", String.class, Object.class);
        }
    }
}
