package broker;

import core.ApiHandler;
import Data.Result;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class TradingAPI {
    public final Broker broker;
    protected final String domain;
    protected final String baseURI;

    protected ArrayList<Header> headers;

    public TradingAPI(Broker broker, String domain) {
        this.broker = broker;
        this.domain = domain;
        this.baseURI = domain + broker.commonPath;
        headers = new ArrayList<>(2);
    }

    public abstract Result fetchInstruments();
    public abstract Result fetchOrders();
    public abstract Result fetchAccountCash();
    public abstract Result fetchAccountMeta();

    public abstract Result placeMarketOrder(String ticker, float quantity);

    protected final Result executeGetRequest(String requestURI) {
        try {
            return ApiHandler.executeApiGetRequest(requestURI, headers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final void addHeaderForRequests(String token, Object value) {
        headers.add(new BasicHeader(token, value));
    }

    public final void createHeaders(HashMap<String, Object> tokens) {
        for (var entry : tokens.entrySet()) {
            headers.add(new BasicHeader(entry.getKey(), entry.getValue()));
        }
    }
}
