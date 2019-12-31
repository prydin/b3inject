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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.jar.JarFile;

public class BootLoader {
    public static class AgentBootException extends Exception {
        public AgentBootException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static void premain(String agentArgs, Instrumentation inst) throws AgentBootException {
        try {
            // Copy the core jar from the resources in the agent jar to a temporary directory so we
            // can append it to the boot classloader.
            File jarFile = File.createTempFile("b3inject-", ".jar");
            System.err.println("Created temporary jarfile: " + jarFile.getAbsolutePath());
           // jarFile.deleteOnExit();
            try (InputStream rs = BootLoader.class.getClassLoader().getResourceAsStream("b3inject-core-1.0.1-jar-with-dependencies.jar")) {
                try (OutputStream out = new FileOutputStream(jarFile)) {
                    byte[] buffer = new byte[1024 * 1024];
                    int n;
                    while ((n = rs.read(buffer)) > 0) {
                        out.write(buffer, 0, n);
                    }
                }
            }

            // Add the newly created jar to the boot class path.
            inst.appendToBootstrapClassLoaderSearch(new JarFile(jarFile));
            // inst.appendToSystemClassLoaderSearch(new JarFile(jarFile));


            // Now use reflection to load the actual agent. This allows us to have zero dependencies to anything but
            // core java.
            Class<?> agentClass = BootLoader.class.getClassLoader().loadClass("net.virtualviking.b3inject.Agent");
            System.out.println(agentClass.getClassLoader());
            System.out.println(BootLoader.class.getClassLoader());
            Method m = agentClass.getMethod("premain", String.class, Instrumentation.class);
            m.invoke(null, agentArgs, inst);

            inst.appendToBootstrapClassLoaderSearch(new JarFile(jarFile));
        } catch(Exception e) {
            throw new AgentBootException("Error while loading B3inject agent: ", e);
        }
    }
}
