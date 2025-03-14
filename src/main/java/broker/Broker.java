package broker;

import core.Result;

public enum Broker {
    TRADING212("Trading 212", "https://demo.trading212.com", "", "/api/v0/equity"),
    ALPACA("Alpaca", "https://paper-api.alpaca.markets", "https://api.alpaca.markets", "/v2");

    public final String name;
    public final String demo;
    public final String live;
    public final String commonPath;

    Broker (String name, String demo, String live, String commonPath){
        this.name = name;
        this.demo = demo;
        this.live = live;
        this.commonPath = commonPath;
    }

}
