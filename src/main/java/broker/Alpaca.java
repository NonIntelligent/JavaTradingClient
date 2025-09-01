package broker;

import Data.Result;

public class Alpaca extends TradingAPI {

    public Alpaca(String domain) {
        super(Broker.ALPACA, domain);
    }

    @Override
    public Result fetchInstruments() {
        final String requestURI = baseURI + "/assets";
        return executeGetRequest(requestURI);
    }

    @Override
    public Result fetchPositions() {
        final String requestURI = baseURI + "/positions";
        return executeGetRequest(requestURI);
    }

    @Override
    public Result fetchOrders() {
        final String requestURI = baseURI + "/orders?status=all";
        return executeGetRequest(requestURI);
    }

    @Override
    public Result fetchAccountCash() {
        final String requestURI = baseURI + "/account";
        return executeGetRequest(requestURI);
    }

    @Override
    public Result fetchAccountMeta() {
        final String requestURI = baseURI + "/account";
        return executeGetRequest(requestURI);
    }

    @Override
    public Result placeMarketOrder(String ticker, float quantity) {
        final String requestURI = baseURI + "/";
        return executeGetRequest(requestURI);
    }
}
