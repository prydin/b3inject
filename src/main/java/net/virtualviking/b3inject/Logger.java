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

import java.util.Map;
import java.util.stream.Collectors;

/**
 * We don't want to drag in any of the larger logging packages into the Java Agent, so we use this tiny one instead.
 */
public class Logger {
    static boolean debugEnabled = "true".equalsIgnoreCase(System.getProperty("b3inject.debug"));

    public static void debug(String s) {
        if(debugEnabled) {
            System.err.println("B3inject DEBUG: " + s);
        }
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static String mapToString(Map<?, ?> map) {
        String mapAsString = map.keySet().stream()
                .map(key -> key + "=" + map.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
        return mapAsString;
    }
}
