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

import javassist.*;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.text.MessageFormat;
import java.util.List;

public class B3InjectTransformer implements ClassFileTransformer {

    private final List<HandlerRule> rules;

    private static final MessageFormat ingressEntry = new MessageFormat(
            "net.virtualviking.b3inject.CallListener.onIngressEntry(\"{0}\", \"{1}\", {2}, $args);");

    private static final MessageFormat ingressExit = new MessageFormat(
            "net.virtualviking.b3inject.CallListener.onIngressExit({0});");

    private static final MessageFormat egressEntry = new MessageFormat(
            "net.virtualviking.b3inject.CallListener.onEgressEntry(\"{0}\", \"{1}\", {2}, $args);");

    private static final MessageFormat egressExit = new MessageFormat(
            "net.virtualviking.b3inject.CallListener.onEgressExit({0});");


    public B3InjectTransformer(List<HandlerRule> rules) {
        CallListener.touch(); // Just to make sure we have a reference to this class somewhere or it may not get loaded
        this.rules = rules;
    }

    private static WildcardFileFilter[] createFilters(List<String> strings) {
        WildcardFileFilter[] result = new WildcardFileFilter[strings.size()];
        for(int idx = 0; idx < strings.size(); ++idx) {
            result[idx] = new WildcardFileFilter(strings.get(idx));
        }
        return result;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassPool pool = ClassPool.getDefault();
            pool.appendClassPath(new LoaderClassPath(loader));
            pool.appendSystemPath();
            CtClass clazz = pool.makeClass(new ByteArrayInputStream(classfileBuffer));

            //System.err.println(clazz.getName());
            boolean touched = false;
            for (CtBehavior behavior : clazz.getDeclaredBehaviors()) {
                if (behavior.isEmpty()
                        || Modifier.isNative(behavior.getModifiers())
                        || behavior.getName().equals("<clinit>")
                        || behavior.getName().startsWith("access$")) {
                    continue;
                }
                boolean absolute = true;
                String fullMethodName = className.replace('/', '.') + "." + behavior.getName() + "(";
                boolean comma = false;
                for(CtClass p : behavior.getParameterTypes()) {
                    if(comma) {
                        fullMethodName += ",";
                    }
                    fullMethodName += p.getName();
                    comma = true;
                }
                fullMethodName += ")";
                //System.err.println(fullMethodName);
                HandlerRule rule = matchRule(fullMethodName);
                if(rule == null) {
                    continue;
                }
                int index = CallListener.registerHandler(rule.getHandler());

                System.err.println("Instrumenting method: " + fullMethodName + " mods=" + behavior.getModifiers());
                try {
                    touched |= instrument(behavior, absolute ? fullMethodName : behavior.getName(), fullMethodName, index, rule.getHandler().isIngress());
                } catch (CannotCompileException e) {
                    System.err.println("Instrumentation failed: " + e.toString());
                }
            }
            return touched ? clazz.toBytecode() : null;
        } catch (Exception e) {
            System.err.println("Instrumentation failed: " + e.toString());
            throw new RuntimeException("Error instrumenting class " + className);
        }
    }

    private boolean instrument(CtBehavior behavior, String methodName, String fullMethodName, int index, boolean ingress)
            throws CannotCompileException {
        Object entryArgs = new Object[]{ methodName, fullMethodName, index };
        Object exitArgs = new Object[] { index };
        if(ingress) {
            behavior.insertBefore(ingressEntry.format(entryArgs));
            behavior.insertAfter(ingressExit.format(exitArgs), true);
        } else {
            behavior.insertBefore(egressEntry.format(entryArgs));
            behavior.insertAfter(egressExit.format(exitArgs), true);
        }
        return true;
    }

    private HandlerRule matchRule(String name) {
        File f = new File(name);
       for(HandlerRule rule : rules) {
           if(rule.getPattern().accept(f)) {
               return rule;
           }
       }
       return null;
    }
}