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

import org.apache.commons.io.filefilter.WildcardFileFilter;

public class HandlerRule {
    private final WildcardFileFilter pattern;

    private final CallHandler handler;


    public HandlerRule(String pattern, CallHandler handler) {
        this.pattern = new WildcardFileFilter(pattern);
        this.handler = handler;
    }

    public WildcardFileFilter getPattern() {
        return pattern;
    }

    public CallHandler getHandler() {
        return handler;
    }
}
