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

    public Quote() {
    }

    @JsonProperty
    public String getSymbol() {
        return symbol;
    }

    @JsonProperty
    public double getPrice() {
        return price;
    }


    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Quote(symbol: " + symbol + ", price: " + price + ")";
    }
}
