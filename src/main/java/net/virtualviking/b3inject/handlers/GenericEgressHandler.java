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

public class GenericEgressHandler {
    public static void enter(Object rq, String methodName) {
        enter(rq, methodName, String.class, String.class);
    }

    public static void enter(Object rq, String methodName, Class<?>... argTypes) {
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
            Method m = rq.getClass().getMethod(methodName, argTypes);
            m.setAccessible(true);
            for(String h : Constants.b3Headers) {
                String value = context.getB3Headers().get(h);
                if (value == null) {
                    continue;
                }
                m.invoke(rq, h, value);
            }
        } catch (NoSuchMethodException|IllegalAccessException| InvocationTargetException e){
            e.printStackTrace();
        }
    }

    public static void exit() {
        Context context = Context.Factory.getContext();
        if(context == null) {
            return;
        }
        context.setEgressHandled(false);
    }
}
