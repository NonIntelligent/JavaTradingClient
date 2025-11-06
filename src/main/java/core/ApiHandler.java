package core;

import Data.Result;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A static class that executes all API requests using a {@code HTTPClient} and returns the HTTP result.
 */
public final class ApiHandler {
    private static final Logger log = LoggerFactory.getLogger("api");
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    // TODO Convert to regular class initialised by Manager,
    //  and passed as dependency to TradingApi class
    private ApiHandler() {
    }

    /**
     * Sends an HTTP GET request to the target API location as specified by the headers.
     * @param request The URL resource location to GET data from.
     * @param headers The HTTP headers for authentication.
     * @return The HTTP response as an object.
     * @throws IOException When the request could not be executed.
     */
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

    /**
     * @param request The URL resource location to GET data from.
     * @param payload The data to send to the server to change data.
     * @param headers The HTTP headers for authentication.
     * @return The HTTP response as an object.
     * @throws IOException When the request could not be executed.
     */
    public static Result executeApiPostRequest(String request, String payload, ArrayList<Header> headers) throws IOException {
        HttpPost httpPost = new HttpPost(request);

        httpPost.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

        httpPost.addHeader("Content-Type", "application/json");

        for (int i = 0; i < headers.size(); i++) {
            httpPost.addHeader(headers.get(i));
        }

        Result result = httpClient.execute(httpPost, response -> {
            log.info("{}->{}", httpPost, new StatusLine(response));
            return new Result(response.getCode(), EntityUtils.toString(response.getEntity()));
        });

        log.info(result.content());

        return result;
    }

    /**
     * Closes the HTTP Client.
     */
    public static void terminate() {
        try {
            log.info("Terminating httpClient");
            httpClient.close();
        } catch (IOException e) {
            log.warn("Problem with closing Http client.", e);
        }
    }
}
