package net.virtualviking.b3inject.examples.quoter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "quoter")
public class Config {
    private String frontendHost;

    private int frontendPort;

    public String getFrontendHost() {
        return frontendHost;
    }

    public void setFrontendHost(String frontendHost) {
        this.frontendHost = frontendHost;
    }

    public int getFrontendPort() {
        return frontendPort;
    }

    public void setFrontendPort(int frontendPort) {
        this.frontendPort = frontendPort;
    }
}
