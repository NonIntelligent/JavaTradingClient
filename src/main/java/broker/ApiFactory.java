package broker;

public final class ApiFactory {
    public static TradingAPI getApi(Broker broker, AccountType type, String key, String apiID) throws IllegalArgumentException {
        String domain = type == AccountType.DEMO ? broker.demo : broker.live;
        TradingAPI api;

        return switch (broker) {
            case TRADING212 -> {
                api = new Trading212(domain);
                api.addHeaderForRequests("Authorization", key);
                yield api;
            }
            default -> throw new IllegalArgumentException("The API factory does not support this broker");
        };
    }
}
