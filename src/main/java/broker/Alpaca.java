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
    public Result fetchStockData(String[] tickers) {
        String allSymbols = String.join("%2C", tickers);
        String requestURI = "https://data.alpaca.markets/v2/stocks/quotes/latest?symbols=" + allSymbols;
        return executeGetRequest(requestURI);
    }

    @Override
    public Result placeMarketOrder(String ticker, String quantity, OrderType orderType, boolean isBuy) {
        final String requestURI = baseURI + "/orders";
        String alpacaExecution = orderType.name().toLowerCase();
        String type = String.format("{\"type\":\"%s\"", alpacaExecution);
        String timeInForce = String.format("\"time_in_force\":\"day\"");
        String symbolName = String.format("\"symbol\":\"%s\"", ticker);
        String amount = String.format("\"qty\":\"%s\"", quantity);
        String buyOrSell = isBuy ? "buy" : "sell";
        String side = String.format("\"side\":\"%s\"}", buyOrSell);

        String payload = String.join(",",
                type, timeInForce, symbolName, amount, side);

        return executePostRequest(requestURI, payload);
    }
}
