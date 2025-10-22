package broker;

/**
 * Use to create the desired Trading API using {@link #getApi(Broker, AccountType, String, String)}.
 */
public final class ApiFactory {
    private ApiFactory (){}

    /**
     * @param broker The broker to connect to.
     * @param type If the account setup as DEMO or LIVE.
     * @param key The secret API key provided by the broker.
     * @param keyID Optional public key if needed by the broker.
     * @return The Trading API interface that is linked to the broker service and so perform API requests.
     * @throws IllegalArgumentException If the given Broker is not handled by this service.
     */
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
            case DEMO -> {
                api = new DemoAPI(domain);
                api.addHeaderForRequests("Auth", key);
                yield api;
            }
            default -> throw new IllegalArgumentException("The API factory does not support the broker " + broker);
        };
    }
}
