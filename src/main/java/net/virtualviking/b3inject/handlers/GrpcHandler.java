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

import io.grpc.*;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.virtualviking.b3inject.Logger;
import net.virtualviking.b3inject.Matchers;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class GrpcHandler {
    public static AgentBuilder buildAgent(final AgentBuilder b) {
        return b
                .type(named("io.grpc.util.MutableHandlerRegistry"))
                .transform((builder, type, loader, module) ->
                        builder.visit(Advice
                                .to(Ingress.class)
                                .on(new Matchers.WildcardMethodMatcher("addService(io.grpc.ServerServiceDefinition)"))))
                .type(hasSuperType(named("io.grpc.ServerBuilder")))
                .transform((builder, type, loader, module) ->
                        builder.visit(Advice
                                .to(Ingress.class)
                                .on(new Matchers.WildcardMethodMatcher("addService(io.grpc.ServerServiceDefinition)"))))
                .type(hasSuperType(named("io.grpc.stub.AbstractStub")))
                .transform(((builder, type, loader, module) ->
                        builder.visit(Advice.to(Stub.class).on(named("getChannel")))));

    }

    public static class Ingress {
        @Advice.OnMethodEnter
        public static void enter(final @Advice.Origin String origin, @Advice.Argument(value = 0, readOnly = false, typing = Assigner.Typing.STATIC) ServerServiceDefinition service) {
            Logger.debug("Entering instrumentation on " + origin);
            service = GrpcFilters.addIngressFilter(service);
        }
    }

    public static class Stub {
        @Advice.OnMethodExit
        public static void exit(final @Advice.Origin String origin, @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object returned) {
            Logger.debug("Entering instrumentation on " + origin);
            returned = ClientInterceptors.intercept((Channel) returned, new GrpcFilters.B3ClientHeaderInjector());
        }
    }
}
