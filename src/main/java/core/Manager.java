package core;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import utility.Settings;
import utility.Timing;

import java.util.ArrayList;

public class Manager {

    public ArrayList<String> instruments;

    Manager() {
        instruments = new ArrayList<>(100);
        JsonFactory factory = JsonFactory.builder()
                .build();
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
        //if (result.isOK())

    }

}
