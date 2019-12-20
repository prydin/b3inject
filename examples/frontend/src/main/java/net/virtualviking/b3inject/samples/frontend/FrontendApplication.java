package net.virtualviking.b3inject.samples.frontend;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class FrontendApplication extends Application<FrontendConfig> {
    @Override
    public String getName() {
        return "frontend";
    }

    @Override
    public void initialize(Bootstrap<FrontendConfig> bootstrap) {
        super.initialize(bootstrap);
    }

    @Override
    public void run(FrontendConfig frontendConfig, Environment environment) {
        final FrontendResource resource = new FrontendResource(
                frontendConfig.backendHost,
                frontendConfig.backendPort,
                frontendConfig.traderHost,
                frontendConfig.traderPort);
        environment.jersey().register(resource);
    }

    public static void main(String[] args) throws Exception {
        new FrontendApplication().run(args);
    }
}
