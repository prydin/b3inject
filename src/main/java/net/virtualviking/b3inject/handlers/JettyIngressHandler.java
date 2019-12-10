package net.virtualviking.b3inject.handlers;

import net.virtualviking.b3inject.CallHandler;
import net.virtualviking.b3inject.Constants;
import net.virtualviking.b3inject.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Map;

public class JettyIngressHandler implements CallHandler {
    @Override
    public void before(Object[] args, Context context) {
        try {
            Map<String, String> headers = context.getB3Headers();
            Object rq = args[1];
            Method m = rq.getClass().getMethod("getHeaders", new Class[]{String.class});
            for(String h : Constants.beHeaders) {
                Enumeration<String> e = (Enumeration<String>) m.invoke(rq, h);
                if(!e.hasMoreElements()) {
                    continue;
                }
               headers.put(h, e.nextElement());
            }

        } catch (NoSuchMethodException|IllegalAccessException|InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void after(Context context) {
    }

    @Override
    public boolean isIngress() {
        return true;
    }
}
