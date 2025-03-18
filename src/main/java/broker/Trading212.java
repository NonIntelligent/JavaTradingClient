package broker;

import Data.Result;

public class Trading212 extends TradingAPI {
    // TODO consider moving JSON parsing to Trading API code?
    public Trading212(String domain) {
        super(Broker.TRADING212, domain);
    }

    @Override
    public Result fetchInstruments() {
        final String requestURI = baseURI + "/metadata/instruments";
        return executeGetRequest(requestURI);
    }

    @Override
    public Result fetchOrders() {
        final String requestURI = baseURI + "/portfolio";
        return executeGetRequest(requestURI);
    }

    @Override
    public Result fetchAccountCash() {
        final String requestURI = baseURI + "/account/cash";
        return executeGetRequest(requestURI);
    }

    @Override
    public Result fetchAccountMeta() {
        final String requestURI = baseURI + "/account/info";
        return executeGetRequest(requestURI);
    }

}
