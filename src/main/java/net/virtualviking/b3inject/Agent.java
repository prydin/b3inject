/*
 *  Copyright 2017 Pontus Rydin
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

import net.virtualviking.b3inject.handlers.ApacheEgressHandler;
import net.virtualviking.b3inject.handlers.JettyIngressHandler;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        List<HandlerRule> rules = new ArrayList<>();

        // Ingress rules
        rules.add(new HandlerRule("org.eclipse.jetty.server.handler.HandlerWrapper.handle(java.lang.String,org.eclipse.jetty.server.Request,"+
                "javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)",
                new JettyIngressHandler()));

        // Egress rules
        rules.add(new HandlerRule("org.apache.http.*.doExecute(" +
                "org.apache.http.HttpHost,org.apache.http.HttpRequest,org.apache.http.protocol.HttpContext)",
                new ApacheEgressHandler()));
        inst.addTransformer(new B3InjectTransformer(rules));
    }
}