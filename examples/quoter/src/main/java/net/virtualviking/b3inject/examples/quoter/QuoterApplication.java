package net.virtualviking.b3inject.examples.quoter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(Config.class)
public class QuoterApplication {
    @Autowired
    private Config config;

    public static void main(String[] args) {
        SpringApplication.run(QuoterApplication.class, args);
    }
}
