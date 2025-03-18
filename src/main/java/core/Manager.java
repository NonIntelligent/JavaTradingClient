package core;

import Data.Instrument;
import Data.Order;
import Data.Position;
import Data.Result;
import broker.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import utility.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Manager {
    private final App app;
    private ObjectMapper mapper;
    public List<String> instruments;
    public List<Account> accounts;

    Manager(App app) {
        this.app = app;
        // Possibly get rid of this copy of instruments.
        // Only UI needs to retain all the instruments to display
        instruments = new ArrayList<>(100);
        accounts = new ArrayList<>();
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public boolean isAPIKeyValid(String apiKey, String broker) {
        if (apiKey.equals("EMPTY")) return false;

        // TODO change to check Account Cash or other low limited rates
        Result result = new Result(401, "TODO");

        return result.status() == 200;
    }

    public Account createAccount() {
        return new Account("",null,null);
    }

    public void reloadState() {
        // TODO load account data from saved settings
        // i.e. load auth keys and re-create account objects
    }

    public void beginProcessing() {
        // TODO replace with timer to check for valid API
        // Then setup tasks and refresh data based on interval

        // TODO load auth data from file if it exists, else do nothing
        //  get broker name/enum that the api is linked to
        //  get Account Type from connection Controller checkbox.
        Broker broker = Broker.TRADING212;
        AccountType type = AccountType.DEMO;
        String key = Settings.getInstance().getApiKey();
        String apiID = "";

        TradingAPI api = ApiFactory.getApi(broker, type, key, apiID);
        Account acc = new Account(key, api, type);

        Result result = acc.tradingApi.fetchInstruments();

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

        result = acc.tradingApi.fetchOrders();

        /* TODO setup json parsing to organise list of instruments and send to FXLoader.
        FxLoader to LandingController to display */
        if (result.isOK()) {
            try {
                Position[] position = mapper.readValue(result.content(), Position[].class);
                app.updateOrders(position);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

}
