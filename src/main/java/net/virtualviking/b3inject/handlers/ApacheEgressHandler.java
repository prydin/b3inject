package net.virtualviking.b3inject.handlers;

import net.virtualviking.b3inject.CallHandler;
import net.virtualviking.b3inject.Constants;
import net.virtualviking.b3inject.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ApacheEgressHandler implements CallHandler {
    @Override
    public void before(Object[] args, Context context) {
        Object rq = args[1];
        try {
            Method m = rq.getClass().getMethod("setHeader", new Class[]{String.class, String.class});
            for(String h : Constants.beHeaders) {
                String value = context.getB3Headers().get(h);
                if (value == null) {
                    continue;
                }
                m.invoke(rq, h, value);
            }
        } catch (NoSuchMethodException|IllegalAccessException|InvocationTargetException e){
            e.printStackTrace();
        }
    }

    @Override
    public void after(Context context) {
    }

    @Override
    public boolean isIngress() {
        return false;
    }
}
