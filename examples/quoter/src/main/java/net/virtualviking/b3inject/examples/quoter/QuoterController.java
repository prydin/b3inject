package net.virtualviking.b3inject.examples.quoter;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RestController
public class QuoterController {

    @RequestMapping(value = "/quoter/symbols", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> index() {
        return List.of("VMW", "AMZN", "GOOG");
    }

    @RequestMapping(value = "/quoter/quote", produces = MediaType.APPLICATION_JSON_VALUE)
    public Quote quote(String symbol) {
        return new Quote(symbol, 123.0);
    }
}