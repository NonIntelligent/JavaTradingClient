package core;

import Data.Instrument;
import Data.Position;
import Data.Result;
import broker.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.util.Pair;
import utility.Consumer;
import utility.Settings;
import utility.TaskExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Manager implements Consumer {
    private final EventChannel eventChannel;
    private ObjectMapper mapper;
    public List<String> instruments;
    public List<Account> accounts;
    private Account activeAccount;
    private final TaskExecutor dataRequester;

    Manager(EventChannel eventChannel) {
        this.eventChannel = eventChannel;
        // Possibly get rid of this copy of instruments.
        // Only UI needs to retain all the instruments to display
        instruments = new ArrayList<>(100);
        accounts = new ArrayList<>();
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        dataRequester = new TaskExecutor(4);
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
        activeAccount = acc;

        accounts.add(acc);

        Result result = acc.tradingApi.fetchInstruments();

        /* TODO setup json parsing to organise list of instruments and send to FXLoader.
        FxLoader to LandingController to display */
        if (result.isOK()) {
            try {
                Instrument[] instruments = mapper.readValue(result.content(), Instrument[].class);
                eventChannel.publish(instruments, AppEventType.ALL_INSTRUMENTS);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

        dataRequester.scheduleDelayedTask(this::retrieveOpenOrders, 500L, 5000L);

    }

    public void retrieveOpenOrders() {
        Result result = activeAccount.tradingApi.fetchOrders();

        /* TODO setup json parsing to organise list of instruments and send to FXLoader.
        FxLoader to LandingController to display */
        if (result.isOK()) {
            try {
                Position[] position = mapper.readValue(result.content(), Position[].class);
                eventChannel.publish(position, AppEventType.OPEN_POSITIONS);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public void processEvent(AppEvent event) {
        switch (event.type()) {
            case MARKET_ORDER -> {Pair<String, Float> a = (Pair<String, Float>) event.data();
                placeMarketOrder(a.getKey(), a.getValue());}
        }
    }

    public void placeMarketOrder(String id, float quantity) {
        activeAccount.tradingApi.placeMarketOrder(id, quantity);
    }

    public void stop() {
        dataRequester.shutdown();
    }
}
