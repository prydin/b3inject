package net.virtualviking.b3inject;

import java.util.HashMap;
import java.util.Map;

public class Context {
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
