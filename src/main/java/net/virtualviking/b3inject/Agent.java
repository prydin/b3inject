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

import net.virtualviking.b3inject.handlers.*;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;

public class Agent {

    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        boolean instumentCore = "true".equalsIgnoreCase(System.getProperty("b3inject.instrumentcore"));
        List<HandlerRule> rules = new ArrayList<>();

        // Ingress rules
        rules.add(new HandlerRule("org.eclipse.jetty.server.handler.HandlerWrapper.handle(" +
                "java.lang.String,org.eclipse.jetty.server.Request,"+
                "javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)",
                new JettyIngressHandler()));
        rules.add(new HandlerRule("javax.servlet.http.HttpServlet.service(" +
                "javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)",
                new SpringIngressHandler()));

        // Egress rules
        rules.add(new HandlerRule("org.apache.http.*.doExecute(" +
                "org.apache.http.HttpHost,org.apache.http.HttpRequest,org.apache.http.protocol.HttpContext)",
                new GenericEgressHandler(1, "setHeader")));
        rules.add(new HandlerRule("org.springframework.http.client.*.executeInternal(" +
                "org.springframework.http.HttpHeaders)",
                new GenericEgressHandler(0, "set")));
        if(instumentCore) {
            rules.add(new HandlerRule("sun.net.www.http.HttpClient.writeRequests(" +
                    "sun.net.www.MessageHeader,sun.net.www.http.PosterOutputStream)",
                    new GenericEgressHandler(0, "set")));
            rules.add(new HandlerRule("sun.net.www.http.HttpClient.writeRequests(" +
                    "sun.net.www.MessageHeader,sun.net.www.http.PosterOutputStream,boolean)",
                    new GenericEgressHandler(0, "set")));
        }
        inst.addTransformer(new B3InjectTransformer(rules));
    }
}