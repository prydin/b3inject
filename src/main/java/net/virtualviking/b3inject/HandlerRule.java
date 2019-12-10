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
