package net.virtualviking.b3inject;

public interface CallHandler {
    void before(Object[] args, Context context);

    void after(Context context);

    boolean isIngress();
}
