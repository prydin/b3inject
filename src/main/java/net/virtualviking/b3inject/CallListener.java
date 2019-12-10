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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CallListener {
    private static ThreadLocal<Context> threadData = new ThreadLocal<>();

    private static List<CallHandler> handlers = new ArrayList<>();

    public static synchronized int registerHandler(CallHandler handler) {
        handlers.add(handler);
        return handlers.size() - 1;
    }

    public static void onIngressEntry(String method, String fullMethodName, int handlerIndex, Object[] parameters) {
        System.out.println(method + " " + handlerIndex);
        if(handlerIndex < handlers.size()) {
            Context ctx = new Context();
            handlers.get(handlerIndex).before(parameters, ctx);
            threadData.set(ctx);
            System.out.println("B3 headers captured:");
            for(Map.Entry<String, String> e : ctx.getB3Headers().entrySet()) {
                System.out.println(e.getKey() + "->" + e.getValue());
            }
        }
    }

    public static void onIngressExit(int handlerIndex) {
        if(handlerIndex < handlers.size()) {
            Context ctx = threadData.get();
            if(ctx != null) {
                handlers.get(handlerIndex).after(ctx);
                threadData.set(null);
            }
        }
    }

    public static void onEgressEntry(String method, String fullMethodName, int handlerIndex, Object[] parameters) {
        System.out.println(method + " " + handlerIndex);
        Context ctx = threadData.get();
        if(ctx == null) {
            return;
        }
        if(handlerIndex < handlers.size()) {
            handlers.get(handlerIndex).before(parameters, ctx);
        }
        System.out.println("B3 headers forwarded:");
        for(Map.Entry<String, String> e : ctx.getB3Headers().entrySet()) {
            System.out.println(e.getKey() + "->" + e.getValue());
        }
    }

    public static void onEgressExit(int handlerIndex) {
        if(handlerIndex < handlers.size()) {
            Context ctx = threadData.get();
            if(ctx != null) {
                handlers.get(handlerIndex).after(ctx);
            }
        }
    }

    static void touch() {
        // Dummy function called just to make sure we're not optimized away.
    }
}
