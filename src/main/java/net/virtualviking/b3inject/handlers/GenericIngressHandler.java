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

import net.virtualviking.b3inject.Constants;
import net.virtualviking.b3inject.Context;
import net.virtualviking.b3inject.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Map;

public class GenericIngressHandler {
    public static void enter(Object rq, String methodName) {
        Context context = Context.Factory.newContext();
        try {
            Map<String, String> headers = context.getB3Headers();
            Method m = rq.getClass().getMethod(methodName, String.class);
            for(String h : Constants.b3Headers) {
                @SuppressWarnings("unchecked")
                String s = (String) m.invoke(rq, h);
                if(s != null) {
                    headers.put(h, s);
                }
            }
            Logger.debug("INGRESS: Captured B3 headers: " + Logger.mapToString(headers));
        } catch (NoSuchMethodException|IllegalAccessException|InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void exit() {
        Context.Factory.clearContext();
    }
}
