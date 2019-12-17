package net.virtualviking.b3inject.examples.quoter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
public class QuoterController {
    @Autowired
    private Config config;

    @RequestMapping(value = "/quoter/symbols", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> index() {
        return List.of("VMW", "AMZN", "GOOG");
    }

    @RequestMapping(value = "/quoter/quote", produces = MediaType.APPLICATION_JSON_VALUE)
    public Quote quote(String symbol) {
        return new Quote(symbol, 123.0);
    }

    @RequestMapping(value = "/quoter/quote_callback")
    public void quoteCallback(String symbol) {
        RestTemplate rt = new RestTemplateBuilder().build();
        rt.postForLocation("http://" + config.getFrontendHost() +":" + config.getFrontendPort() + "/callback", new Quote(symbol, 123.0));
    }
}