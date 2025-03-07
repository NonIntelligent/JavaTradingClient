package broker;

public enum Broker {
    TRADING212("Trading 212", "https://demo.trading212.com", "");

    String name;
    String demo;
    String live;

    Broker (String name, String demo, String live){
        this.name = name;
        this.demo = demo;
        this.live = live;
    }
}
