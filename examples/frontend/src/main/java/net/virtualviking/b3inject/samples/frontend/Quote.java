package net.virtualviking.b3inject.samples.frontend;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;

public class Quote {
    @NotEmpty
    private String symbol;

    @NotEmpty
    private double price;

    public Quote(@NotEmpty String symbol, @NotEmpty double price) {
        this.symbol = symbol;
        this.price = price;
    }

    @JsonProperty
    public String getSymbol() {
        return symbol;
    }

    @JsonProperty
    public double getPrice() {
        return price;
    }
}
