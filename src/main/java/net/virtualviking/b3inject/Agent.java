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

package net.virtualviking.b3inject;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.utility.JavaModule;
import net.virtualviking.b3inject.handlers.*;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.none;

public class Agent {

    private static class DebugListener implements AgentBuilder.Listener {
        @Override
        public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
        }

        @Override
        public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
            Logger.debug("Transforming class: " + typeDescription.getActualName());
        }

        @Override
        public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {
        }

        @Override
        public void onError(String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {
            Logger.debug("Error transforming class " + s + ": " + throwable.toString());
        }

        @Override
        public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
        }
    }

    private static AgentBuilder newBuilder() {
        AgentBuilder agentBuilder = new AgentBuilder.Default(new ByteBuddy().with(TypeValidation.DISABLED))
                .disableClassFormatChanges()
                .ignore(none());
        return agentBuilder
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
                .with(AgentBuilder.TypeStrategy.Default.REDEFINE);
    }

    private static void instrument(AgentBuilder b, Instrumentation inst) {
        if(Logger.isDebugEnabled()) {
            b = b.with(new DebugListener());
        }
        b.installOn(inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        instrument(JettyIngressHandler.buildAgent(newBuilder()), inst);
        instrument(ServletIngressHandler.buildAgent(newBuilder()), inst);
        instrument(SpringEgressHandler.buildAgent(newBuilder()), inst);
        instrument(ApacheEgressHandler.buildAgent(newBuilder()), inst);
        instrument(JaxRsEgressHandler.buildAgent(newBuilder()), inst);
     //   instrument(ServletFilterHandler.buildAgent(newBuilder()), inst);
    }
}