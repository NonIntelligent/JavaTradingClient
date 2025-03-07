package core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import broker.Instrument;
import utility.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Manager {
    private final App app;
    private ObjectMapper mapper;
    public List<String> instruments;

    Manager(App app) {
        this.app = app;
        instruments = new ArrayList<>(100);
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public boolean isAPIKeyValid(String apiKey, String broker) {
        if (apiKey.equals("EMPTY")) return false;

        Result result = ApiHandler.getAccountCash(apiKey, broker);

        return result.status() == 200;
    }

    public void beginProcessing() {
        // TODO replace with timer to check for valid API
        // Then setup tasks and refresh data based on interval

        String key = Settings.getInstance().getApiKey();

        Result result = ApiHandler.fetchInstruments(key, "broker");

        /* TODO setup json parsing to organise list of instruments and send to FXLoader.
        FxLoader to LandingController to display */
        if (result.isOK()) {
            try {
                Instrument[] instruments = mapper.readValue(result.content(), Instrument[].class);
                app.updateInstruments(instruments);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

}
