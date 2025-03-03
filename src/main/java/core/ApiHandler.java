package core;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public final class ApiHandler {
    private static final Logger log = LogManager.getLogger("API");
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();


    private ApiHandler() {
    }

    public static Result connectToApi(String apiKey, String broker) {
        broker = "https://demo.trading212.com";
        String request = broker + "/api/v0/equity/portfolio";
        HttpGet httpGet = new HttpGet(request);
        httpGet.addHeader("Authorization", apiKey);

        Result result = null;
        try {
            result = httpClient.execute(httpGet, response -> {
                log.info("{}->{}", httpGet, new StatusLine(response));
                return new Result(response.getCode(), EntityUtils.toString(response.getEntity()));
            });

        } catch (IOException e) {
            log.error("Could not execute GET request.", e);
        }

        log.info(result.content());
        return result;
    }

    public static Result fetchInstruments(String apiKey, String broker) {
        broker = "https://demo.trading212.com";
        String request = broker + "/api/v0/equity/metadata/instruments";
        HttpGet httpGet = new HttpGet(request);
        httpGet.addHeader("Authorization", apiKey);

        Result result = null;
        try {
            result = httpClient.execute(httpGet, response -> {
                log.info("{}->{}", httpGet, new StatusLine(response));
                return new Result(response.getCode(), EntityUtils.toString(response.getEntity()));
            });

        } catch (IOException e) {
            log.error("Could not execute GET request.", e);
        }

        log.info(result.content());
        return result;
    }

    public static Result getAccountCash(String apiKey, String broker) {
        broker = "https://demo.trading212.com";
        String request = broker + "/api/v0/equity/account/cash";
        HttpGet httpGet = new HttpGet(request);
        httpGet.addHeader("Authorization", apiKey);

        Result result = null;
        try {
            result = httpClient.execute(httpGet, response -> {
                log.info("{}->{}", httpGet, new StatusLine(response));
                return new Result(response.getCode(), EntityUtils.toString(response.getEntity()));
            });

        } catch (IOException e) {
            log.error("Could not execute GET request.", e);
        }

        log.info(result.content());
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
