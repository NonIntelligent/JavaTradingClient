package broker;

public final class ApiFactory {
    public static TradingAPI getApi(Broker broker, AccountType type, String key, String keyID) throws IllegalArgumentException {
        String domain = type == AccountType.DEMO ? broker.demo : broker.live;
        TradingAPI api;

        return switch (broker) {
            case TRADING212 -> {
                api = new Trading212(domain);
                api.addHeaderForRequests("Authorization", key);
                yield api;
            }
            case ALPACA ->  {
                api = new Alpaca(domain);
                api.addHeaderForRequests("accept", "application/json");
                api.addHeaderForRequests("APCA-API-KEY-ID", keyID);
                api.addHeaderForRequests("APCA-API-SECRET-KEY", key);
                yield api;
            }
            default -> throw new IllegalArgumentException("The API factory does not support the broker " + broker);
        };
    }
}
