package broker;

public final class ApiFactory {
    public static TradingAPI getApi(Broker broker, AccountType type, String key, String apiID) {
        String domain = type == AccountType.DEMO ? broker.demo : broker.live;
        TradingAPI api;

        return switch (broker) {
            case TRADING212 -> {
                api = new Trading212(domain);
                api.addHeaderForRequests("Authorization", key);
                yield api;
            }
            default -> throw new IllegalArgumentException("Broker case not supported");
        };
    }
}
