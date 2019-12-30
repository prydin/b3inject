package net.virtualviking.b3inject;

import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;

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

public class Matchers {
    public static class WildcardMatcher extends ElementMatcher.Junction.AbstractBase<NamedElement> {
        private final WildcardFileFilter filter;

        public WildcardMatcher(String filter) {
            this.filter = new WildcardFileFilter(filter);
        }

        @Override
        public boolean matches(NamedElement t) {
            return filter.accept(new File(t.getActualName()));
        }
    }

    public static class WildcardMethodMatcher extends ElementMatcher.Junction.AbstractBase<MethodDescription> {
        private final WildcardFileFilter filter;

        public WildcardMethodMatcher(String filter) {
            this.filter = new WildcardFileFilter(filter);
        }

        @Override
        public boolean matches(MethodDescription t) {
            StringBuilder sb = new StringBuilder();
            sb.append(t.getActualName());
            sb.append('(');
            boolean first = true;
            for(ParameterDescription.InDefinedShape p : t.getParameters().asDefined()) {
                if(!first) {
                    sb.append(',');
                }
                sb.append(p.getType().getActualName());
                first = false;
            }
            sb.append(')');
            boolean match =  filter.accept(new File(sb.toString()));
            // Logger.debug(sb.toString() + "==" + filter.toString() + ": " + match);
            return match;
        }
    }

}
