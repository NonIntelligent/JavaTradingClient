package core;

import Data.Instrument;
import Data.Position;
import Data.Result;
import broker.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.AccountApiStore;
import utility.Consumer;
import utility.TaskExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Manager implements Consumer {
    private static final Logger log = LoggerFactory.getLogger("application");
    private final EventChannel eventChannel;
    private AccountApiStore apiStore;
    private ObjectMapper mapper;
    public List<String> instruments;
    private ObservableList<Account> accounts;
    private Account activeAccount;
    private final TaskExecutor dataRequester;

    Manager(EventChannel eventChannel) {
        this.eventChannel = eventChannel;
        // Possibly get rid of this copy of instruments.
        // Only UI needs to retain all the instruments to display
        instruments = new ArrayList<>(100);
        accounts = FXCollections.observableArrayList();
        apiStore = new AccountApiStore(accounts);
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        dataRequester = new TaskExecutor(4);
    }

    public Account createAccountFromJSON(String jsonData) {
        try {
            JsonNode node = mapper.readTree(jsonData);
            Broker broker = Broker.get(node.get("broker").asText());
            AccountType type = AccountType.valueOf(node.get("type").asText());
            String apiKey = node.get("apiKey").asText();
            String apiID = node.get("apiID").asText();

            TradingAPI api = ApiFactory.getApi(broker, type, apiKey, apiID);

            Account newAccount = new Account(api, type, apiKey, apiID);
            activeAccount = newAccount;
            accounts.add(newAccount);

            return newAccount;
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON data for parsing when creating an account\n" + jsonData, e);
        }

        return null;
    }

    public void beginProcessing() {
        log.info("Start processing and fetching api data for the accounts");
        // TODO replace with timer to check for valid API
        // Then setup tasks and refresh data based on interval

        // TODO present user dialog for password entry. (prompt can be ignored).
        try {
            loadAccountsFromCache("");
        } catch (IOException e) {
            log.error("Failed to read account data from JSON file", e);
        }

        if (activeAccount != null) {
            Result result = activeAccount.tradingApi.fetchInstruments();
        /* TODO setup json parsing to organise list of instruments and send to FXLoader.
        FxLoader to LandingController to display */
            if (result.isOK()) {
                try {
                    Instrument[] instruments = mapper.readValue(result.content(), Instrument[].class);
                    eventChannel.publish(instruments, AppEventType.ALL_INSTRUMENTS);
                } catch (IOException e) {
                    log.error("JSON parsing failed to read result contents", e);
                } catch (InterruptedException e) {
                    log.error("Event publishing was interrupted", e);
                }

            }
        }


        dataRequester.scheduleDelayedTask(this::retrieveOpenOrders, 500L, 5000L);
    }

    // Recreates all accounts from the cache. First item loaded is considered active.
    public void loadAccountsFromCache(String password) throws IOException {
        List<ApiData> apiDataList = apiStore.loadStoredAPIs(password);
        if (apiDataList.isEmpty()) {
            // TODO Pop up error about no entries due to bad data or no accounts saved
            log.info("No accounts were loaded");
            return;
        }
        // Create accounts from the loaded data
        for (ApiData data : apiDataList) {
            Account acc = new Account(data);
            accounts.add(acc);
        }

        activeAccount = accounts.getFirst();
    }

    public void retrieveOpenOrders() {
        if (activeAccount == null) return;

        Result result = activeAccount.tradingApi.fetchOrders();

        /* TODO setup json parsing to organise list of instruments and send to FXLoader.
        FxLoader to LandingController to display */
        if (result.isOK()) {
            try {
                Position[] position = mapper.readValue(result.content(), Position[].class);
                eventChannel.publish(position, AppEventType.OPEN_POSITIONS);
            } catch (IOException e) {
                log.error("JSON parsing failed to read result contents", e);
            } catch (InterruptedException e) {
                log.error("Event publishing was interrupted", e);
            }

        }
    }

    @Override
    public void processEvent(AppEvent event) {
        switch (event.type()) {
            case MARKET_ORDER -> {Pair<String, Float> a = (Pair<String, Float>) event.data();
                placeMarketOrder(a.getKey(), a.getValue());}
            case CREATE_ACCOUNT -> {
                createAccountFromJSON((String) event.data());}
        }
    }

    public void placeMarketOrder(String id, float quantity) {
        activeAccount.tradingApi.placeMarketOrder(id, quantity);
    }

    public void stop() {
        dataRequester.shutdown();
        try {
            apiStore.saveAPIsToFile(accounts);
        } catch (IOException e) {
            log.error("Could not save api data to file", e);
        }
    }

    public ObservableList<Account> getAccounts() {return accounts;}
}
