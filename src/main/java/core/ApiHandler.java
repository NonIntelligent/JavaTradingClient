package core;

import Data.Result;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

public final class ApiHandler {
    private static final Logger log = LogManager.getLogger("API");
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    // TODO Convert to regular class initialised by Manager,
    //  and passed as dependency to TradingApi class
    private ApiHandler() {
    }

    public static Result executeApiGetRequest(String request, ArrayList<Header> headers) throws IOException {
        HttpGet httpGet = new HttpGet(request);

        for (int i = 0; i < headers.size(); i++) {
            httpGet.addHeader(headers.get(i));
        }

        Result result = httpClient.execute(httpGet, response -> {
            log.info("{}->{}", httpGet, new StatusLine(response));
            return new Result(response.getCode(), EntityUtils.toString(response.getEntity()));
        });

        return result;
    }

    public static void shutdown() {
        try {
            httpClient.close();
        } catch (IOException e) {
            log.error("Problem with closing Http client.", e);
        }
    }
}
