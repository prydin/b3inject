package net.virtualviking.b3inject.samples.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotEmpty;

public class FrontendConfig extends Configuration {
    @NotEmpty
    String backendHost;

    int backendPort;

    String traderHost;

    int traderPort;

    @JsonProperty
    public String getBackendHost() {
        return backendHost;
    }

    @JsonProperty
    public void setBackendHost(String backendHost) {
        this.backendHost = backendHost;
    }

    @JsonProperty
    public int getBackendPort() {
        return backendPort;
    }

    @JsonProperty
    public void setBackendPort(int backendPort) {
        this.backendPort = backendPort;
    }

    @JsonProperty
    public String getTraderHost() { return traderHost; }

    @JsonProperty
    public int getTraderPort() { return traderPort; }
}
