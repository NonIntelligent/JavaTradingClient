package broker;

import Data.Result;

import java.lang.reflect.Method;
import java.util.HashMap;

public class Trading212 extends TradingAPI {
    private HashMap<Method, Long> methodTimeouts;


    // TODO consider moving JSON parsing to Trading API code?
    public Trading212(String domain) {
        super(Broker.TRADING212, domain);

        // TODO idea for storing unique data for each class per individual instance.
        //  maybe move to parent class
        //  data is to be retrieved by the method caller
        methodTimeouts = new HashMap<>();
        try {
            methodTimeouts.put(this.getClass().getMethod("fetchPositions"), 5000L);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Result fetchInstruments() {
        final String requestURI = baseURI + "/metadata/instruments";
        return executeGetRequest(requestURI);
    }

    @Override
    public Result fetchPositions() {
        final String requestURI = baseURI + "/portfolio";
        return executeGetRequest(requestURI);
    }

    @Override
    public Result fetchOrders() {
        final String requestURI = baseURI + "/orders";
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

    @Override
    public Result fetchStockData(String[] tickers) {
        return null;
    }

    @Override
    public Result placeMarketOrder(String ticker, String quantity, OrderType orderType, boolean isBuy) {
        final String requestURI = baseURI + "/orders/market";

        // Create market order of 0.1 quantity.
        String stringQuantity = String.format(" \"quantity\": %.1f,", quantity);
        String stringTicker = String.format(" \"ticker\": \"%s\"", ticker);

        String payload = String.join("\n","{", stringQuantity, stringTicker,"}");

        return executePostRequest(requestURI, payload);
    }

}
