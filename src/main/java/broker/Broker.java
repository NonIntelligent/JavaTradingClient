package broker;

import java.util.HashMap;
import java.util.Map;

public enum Broker {
    TRADING212("Trading 212", "https://demo.trading212.com", "", "/api/v0/equity"),
    ALPACA("Alpaca", "https://paper-api.alpaca.markets", "https://api.alpaca.markets", "/v2");

    public final String name;
    public final String demo;
    public final String live;
    public final String commonPath;

    private static Map<String, Broker> enumMap;

    Broker(String name, String demo, String live, String commonPath){
        this.name = name;
        this.demo = demo;
        this.live = live;
        this.commonPath = commonPath;
    }

    // Fast retrieval of broker based on name.
    static {
        enumMap = new HashMap<>();
        for (Broker b : Broker.values()) {
            enumMap.put(b.name,b);
        }
    }

    public static Broker get(String name){
        return enumMap.get(name);
    }

}
