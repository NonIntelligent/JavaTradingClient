package broker;

import Data.Result;
import com.fasterxml.jackson.core.JsonParser;
import core.ApiHandler;

import java.io.IOException;

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

    @Override
    public Result placeMarketOrder(String ticker, float quantity) {
        final String requestURI = baseURI + "/orders/market";

        String stringQuantity = String.format(" \"quantity\": %.1f,", quantity);
        String stringTicker = String.format(" \"ticker\": \"%s\"", ticker);

        String payload = String.join("\n","{", stringQuantity, stringTicker,"}");

        try {
            return ApiHandler.executeApiPostRequest(requestURI, payload, headers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
