package core;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.StatusLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ApiHandler {
    private final Logger _log = LogManager.getLogger("API");
    private CloseableHttpClient _httpClient;


    public ApiHandler(){
        try (CloseableHttpClient cl = HttpClients.createDefault()) {
        _httpClient = cl;

        } catch (IOException e) {
            _log.error("Could not create Http client. Terminating program", e);
        };


    }

    public void connectToApi(String apiKey, String broker) {
        broker = "https://demo.trading212.com";
        String request = broker + "/api/v0/equity/metadata/exchanges";
        HttpGet httpGet = new HttpGet(request);
        httpGet.addHeader("Authorization", apiKey);

        Result result = null;
        try {
            CloseableHttpClient cl = HttpClients.createDefault();
            result = cl.execute(httpGet, response -> {
                _log.info(httpGet + "->" + new StatusLine(response));
                return new Result(response.getCode(), EntityUtils.toString(response.getEntity()));
            });
        } catch (IOException e) {
            _log.error("Could not execute GET request.", e);
        }

        _log.info(result.content);
    }

    static class Result {
        final int status;
        final String content;

        Result(int status, String content){
            this.status = status;
            this.content = content;
        }
    }
}
