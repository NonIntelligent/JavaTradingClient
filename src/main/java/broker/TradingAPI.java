package broker;

import core.ApiHandler;
import Data.Result;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Superclass for every broker to perform API requests to fetch account, market data and place orders to buy/sell stocks.
 */
public abstract class TradingAPI {
    public final Broker broker;
    protected final String domain;
    protected final String baseURI;

    protected ArrayList<Header> headers;

    /**
     * Set broker type and the common url string for each resource in the API, such as the domain name.
     *
     * @param broker The broker associated with this API.
     * @param domain The domain url for either the demo or live account.
     */
    public TradingAPI(Broker broker, String domain) {
        this.broker = broker;
        this.domain = domain;
        this.baseURI = domain + broker.commonPath;
        headers = new ArrayList<>(2);
    }

    /**
     * Fetch the list of instruments/tickers from the API.
     * @return The JSON list of all instruments including their properties.
     */
    public abstract Result fetchInstruments();

    /**
     * Fetch the active positions on the account.
     * @return The JSON list of positions including price and other meta information.
     */
    public abstract Result fetchPositions();

    /**
     * Fetch all active and closed orders made by the account.
     * @return The JSON list of all orders and their metadata.
     */
    public abstract Result fetchOrders();

    /**
     * Fetch the invested, free and total cash on the account.
     * @return A JSON object containing the cash amounts.
     */
    public abstract Result fetchAccountCash();

    /**
     * Fetch metadata related to the account such as ID or currency code.
     * @return The JSON list of metadata.
     */
    public abstract Result fetchAccountMeta();

    /**
     * Fetch the price data for each specified stock as a list of {@code Quote} objects.
     * @param tickers The list of stocks to get data from.
     * @return A JSON list of {@code Quote} objects with bid and ask price.
     */
    public abstract Result fetchStockData(String[] tickers);

    /**
     * @param ticker The target stock to purchase.
     * @param quantity The amount to buy (#.##).
     * @param orderType The method to execute the order.
     * @param isBuy If this is a buy or sell order.
     * @return If the order was successful or not.
     */
    public abstract Result placeMarketOrder(String ticker, String quantity, OrderType orderType, boolean isBuy);

    /**
     * Execute a get request to the specified resource and providing the auth headers.
     * @param requestURI The API resource location.
     * @return The result returned by the GET request.
     */
    protected final Result executeGetRequest(String requestURI) {
        try {
            return ApiHandler.executeApiGetRequest(requestURI, headers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute a POST request to the resource API alongside the payload to specify the data to change.
     * @param requestURI The API resource location.
     * @param payload The data to send to the server for the request.
     * @return The result of the POST request.
     */
    protected final Result executePostRequest(String requestURI, String payload) {
        try {
            return ApiHandler.executeApiPostRequest(requestURI, payload, headers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add headers to a list to be included for every HTTP request.
     * @param token The token name.
     * @param value The value of the token.
     */
    public final void addHeaderForRequests(String token, Object value) {
        headers.add(new BasicHeader(token, value));
    }

    /**
     * Add all headers from the {@code HashMap}.
     * @param tokens Key-value HashMap of all tokens to include.
     */
    public final void createHeaders(HashMap<String, Object> tokens) {
        for (var entry : tokens.entrySet()) {
            headers.add(new BasicHeader(entry.getKey(), entry.getValue()));
        }
    }
}
