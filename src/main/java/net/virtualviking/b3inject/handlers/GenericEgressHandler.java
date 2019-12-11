package net.virtualviking.b3inject.handlers;

import net.virtualviking.b3inject.CallHandler;
import net.virtualviking.b3inject.Constants;
import net.virtualviking.b3inject.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GenericEgressHandler implements CallHandler {
    private final int argIndex;

    private final String methodName;

    public GenericEgressHandler(int argIndex, String methodName) {
        this.argIndex = argIndex;
        this.methodName = methodName;
    }

    @Override
    public void before(Object[] args, Context context) {
        Object rq = args[argIndex];
        try {
            Method m = rq.getClass().getMethod(methodName, new Class[]{String.class, String.class});
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

    @Override
    public void after(Context context) {
    }

    @Override
    public boolean isIngress() {
        return false;
    }
}
