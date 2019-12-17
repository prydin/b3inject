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
import net.virtualviking.b3inject.Matchers;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class SunJDKEgressHandler {
    private static Class<?> superClass;

    private static class ZeroOffsetMethodEntry {
        @Advice.OnMethodEnter
        public static void enter(final @Advice.Origin String origin, final @Advice.This Object self, final @Advice.AllArguments Object[] args) {
            GenericEgressHandler.enter(args[0], "setHeader");
        }

        @Advice.OnMethodExit
        public static void exit() {
            GenericEgressHandler.exit();
        }
    }

    private static class OneOffsetMethodEntry {
        @Advice.OnMethodEnter
        public static void enter(final @Advice.Origin String origin, final @Advice.This Object self, final @Advice.AllArguments Object[] args) {
            GenericEgressHandler.enter(args[0], "setHeader");
        }

        @Advice.OnMethodExit
        public static void exit() {
            GenericEgressHandler.exit();
        }
    }

    public static AgentBuilder buildAgent(AgentBuilder b) {
        return b.type(hasSuperType(named("org.apache.http.client.HttpClient")))
                .transform((builder, type, classLoader, module) ->
                        builder
                                .visit(Advice.to(ZeroOffsetMethodEntry.class).on(new Matchers.WildcardMethodMatcher(
                                        "execute(org.apache.http.client.methods.HttpUriRequest*)")))
                                .visit(Advice.to(OneOffsetMethodEntry.class).on(new Matchers.WildcardMethodMatcher(
                                        "execute(org.apache.http.HttpHost,org.apache.http.HttpRequest*)"))));
    }
}
