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

import java.util.HashMap;
import java.util.Map;

public class Context {
    public static class Factory {
        private static ThreadLocal<Context> context = new ThreadLocal<>();

        public static Context newContext() {
            Context ctx = new Context();
            context.set(ctx);
            return ctx;
        }

        public static Context getContext() {
            return context.get();
        }

        public static void clearContext() {
            context.set(null);
        }
    }
    private final Map<String, String> b3Headers = new HashMap<>();

    private boolean egressHandled;

    public Context() {
    }

    public Map<String, String> getB3Headers() {
        return b3Headers;
    }

    public boolean isEgressHandled() {
        return egressHandled;
    }

    public void setEgressHandled(boolean egressHandled) {
        this.egressHandled = egressHandled;
    }
}
