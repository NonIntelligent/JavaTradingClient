package core;

import Data.*;
import broker.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.AccountApiStore;
import utility.Consumer;
import utility.TaskExecutor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class Manager implements Consumer {
    private static final Logger log = LoggerFactory.getLogger("application");
    private final EventChannel eventChannel;
    private AccountApiStore apiStore;
    private ObjectMapper mapper;
    public List<String> instruments;
    private ObservableList<Account> accounts;
    private Account activeAccount;
    private final TaskExecutor dataRequester;
    private boolean demoMode = false;

    Manager(EventChannel eventChannel) {
        this.eventChannel = eventChannel;
        // Possibly get rid of this copy of instruments.
        // Only UI needs to retain all the instruments to display
        instruments = new ArrayList<>(100);
        accounts = FXCollections.observableArrayList();
        apiStore = new AccountApiStore();
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        dataRequester = new TaskExecutor(4);
    }

    public Account createAccountFromJSON(Object data) {
        if (data instanceof String jsonData) {
            try {
                JsonNode node = mapper.readTree(jsonData);
                Broker broker = Broker.get(node.get("broker").asText());
                AccountType type = AccountType.valueOf(node.get("type").asText());
                String apiKey = node.get("apiKey").asText();
                String apiID = node.get("apiID").asText();

                TradingAPI api = ApiFactory.getApi(broker, type, apiKey, apiID);

                Account newAccount = new Account(api, type, apiKey, apiID);
                accounts.add(newAccount);
                activeAccount = newAccount;

                beginProcessing();

                return newAccount;
            } catch (JsonProcessingException e) {
                log.error("Invalid JSON data for parsing when creating an account\n" + jsonData, e);
            }

        }
        return null;
    }

    public void beginProcessing() {
        log.info("Start processing and fetching api data for the accounts");
        // TODO replace with timer to check for valid API
        //   Then setup tasks and refresh data based on interval
        dataRequester.cancelAllTasks();

        retrieveAllInstruments(activeAccount);
        dataRequester.scheduleRepeatedDelayTask(this::retrieveOpenPositions, 500L, 5000L);
        dataRequester.scheduleRepeatedDelayTask(this::retrieveOrders, 500L, 10000L);
        dataRequester.scheduleRepeatedDelayTask(this::fetchAccountCash, 100L, 3000L);
        dataRequester.scheduleTask(this::fetchAccountMetadata, 100L);
    }

    // Recreates all accounts from the cache. First item loaded is considered active.
    public void loadAccountsFromCache(String password) {
        List<ApiData> apiDataList = new ArrayList<>();
        try {
            apiDataList = apiStore.loadStoredAPIs(password);
        } catch (IOException e) {
            log.error("Problem with reading JSON data.", e);
        }

        if (apiDataList.isEmpty()) {
            // TODO Pop up error about no entries due to bad data or no accounts saved
            log.info("No accounts were loaded as list was empty.");
            return;
        }
        // Create accounts from the loaded data
        for (ApiData data : apiDataList) {
            Account acc = new Account(data);
            accounts.add(acc);
        }

        activeAccount = accounts.getFirst();
    }

    public void retrieveAllInstruments(Account acc) {
        if (acc != null) {
            // TODO get account metadata and cash for all
            Result result = acc.tradingApi.fetchInstruments();
            if (result.isOK()) {
                try {
                    Instrument[] instruments = mapper.readValue(result.content(), Instrument[].class);
                    ArrayList<Instrument> instrumentsList = new ArrayList<>();

                    for (Instrument inst : instruments) {
                        if (inst.tradable && inst.getType().equals("us_equity")) {
                            instrumentsList.add(inst);
                        }
                    }

                    Instrument[] filteredInstruments = instrumentsList.toArray(new Instrument[0]);

                    eventChannel.publish(filteredInstruments, AppEventType.ALL_INSTRUMENTS, this);
                } catch (IOException e) {
                    log.error("JSON parsing failed to read result contents", e);
                } catch (InterruptedException e) {
                    log.error("Event publishing was interrupted", e);
                }

            }
        }
    }

    public void retrieveOpenPositions() {
        if (activeAccount == null) return;

        Result result = activeAccount.tradingApi.fetchPositions();

        /* TODO setup json parsing to organise list of instruments and send to FXLoader.
        FxLoader to LandingController to display */
        if (result.isOK()) {
            try {
                Position[] position = mapper.readValue(result.content(), Position[].class);
                eventChannel.publish(position, AppEventType.OPEN_POSITIONS, this);
            } catch (IOException e) {
                log.error("JSON parsing failed to read result contents", e);
            } catch (InterruptedException e) {
                log.error("Event publishing was interrupted", e);
            }

        }
    }

    public void retrieveOrders() {
        if (activeAccount == null) return;

        Result result = activeAccount.tradingApi.fetchOrders();

        if (result.isOK()) {
            try {
                Order[] orders = mapper.readValue(result.content(), Order[].class);
                eventChannel.publish(orders, AppEventType.ALL_ORDERS, this);
            } catch (JsonProcessingException e) {
                log.error("JSON parsing failed to read result contents", e);
            } catch (InterruptedException e) {
                log.error("Event publishing was interrupted", e);
            }

        }
    }

    public void fetchAccountCash() {
        if (activeAccount == null) return;

        Result result = activeAccount.tradingApi.fetchAccountCash();

        if (result.isOK()) {
            try {
                // TODO generate cash data type?
                //  Or look to instead update account data directly and let UI react using property
                JsonNode node = mapper.readTree(result.content());
                BigDecimal cash = new BigDecimal(node.get("cash").asText("0.00"));
                cash = cash.setScale(2, RoundingMode.HALF_DOWN);

                BigDecimal total_cash = new BigDecimal(node.get("equity").asText("0.00"));
                total_cash = total_cash.setScale(2, RoundingMode.HALF_DOWN);

                BigDecimal invested = total_cash.subtract(cash);
                invested = invested.setScale(2, RoundingMode.HALF_DOWN);

                activeAccount.freeCash = cash.doubleValue();
                activeAccount.totalCash = total_cash.doubleValue();
                activeAccount.investedCash = invested.doubleValue();
                eventChannel.publish(null, AppEventType.REFRESH_TABLES, this);
            } catch (JsonProcessingException e) {
                log.error("JSON parsing failed to read result contents", e);
            } catch (InterruptedException e) {
                log.error("Event publishing was interrupted", e);
            }

        }
    }

    public void fetchAccountMetadata() {
        if (activeAccount == null) return;

        Result result = activeAccount.tradingApi.fetchAccountMeta();

        if (result.isOK()) {
            try {
                // TODO generate cash data type?
                //  Or look to instead update account data directly and let UI react using property
                JsonNode node = mapper.readTree(result.content());
                String accountID = node.get("id").asText("UUID");
                String currency = node.get("currency").asText("???");
                activeAccount.accountID = accountID;
                activeAccount.currencyCode = currency;
                eventChannel.publish(null, AppEventType.REFRESH_TABLES, this);
            } catch (JsonProcessingException e) {
                log.error("JSON parsing failed to read result contents", e);
            } catch (InterruptedException e) {
                log.error("Event publishing was interrupted", e);
            }

        }
    }

    public void fetchLatestStockQuote(Object instruments) {
        if (activeAccount == null) return;
        Instrument inst = (Instrument) instruments;
        String[] symbols = {inst.symbol};

        Result result = activeAccount.tradingApi.fetchStockData(symbols);

        if (result.isOK()) {
            try {
                Quote quote = mapper.readValue(result.content(), Quote.class);
                eventChannel.publish(quote, AppEventType.LATEST_STOCK_QUOTE, this);
            } catch (JsonProcessingException e) {
                log.error("JSON parsing failed to read result contents", e);
            } catch (InterruptedException e) {
                log.error("Event publishing was interrupted", e);
            }
        }
    }

    private void scheduleStockQuoteRetrieval(Object instruments) {
        Future<?> future = dataRequester.scheduleRepeatedDelayTask(() -> {fetchLatestStockQuote(instruments);},
                100L, 1000L);
        try {
            eventChannel.publish(future, AppEventType.TASK_GET, this);
        } catch (InterruptedException e) {
            dataRequester.cancelTask(future, false);
            log.error("Manager was interrupted and could not send Quote task event to FX Loading", e);
        }
    }

    public void cancelTask(Object future) {
        if (future == null) {
            log.debug("Null future object was sent to Manager");
            return;
        }

        Future<?> task = (Future<?>) future;

        boolean cancelled = dataRequester.cancelTask(task, true);

        log.info("Task cancelled status {}", cancelled);
    }

    public void startDemoMode() {
        dataRequester.cancelAllTasks();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for tasks to cancel. No idea why.", e);
            return;
        }
        String apiSecret = "secret";
        String apiID = "id";

        DemoAPI demo = (DemoAPI) ApiFactory.getApi(Broker.DEMO, AccountType.DEMO, apiSecret, apiID);
        Account demoAccount = new Account(demo, AccountType.DEMO, apiSecret, apiID);
        accounts.add(demoAccount);
        activeAccount = demoAccount;

        demoMode = true;
        beginProcessing();
    }

    @Override
    public void processEvent(AppEvent event) {
        switch (event.type()) {
            case DEMO_APP -> {startDemoMode();}
        }

        if (event.data() == null) {
            log.error("AppEvent data is NULL of type {}", event.type());
            return;
        }

        switch (event.type()) {
            case MARKET_ORDER_BUY -> {placeMarketOrder(event.data(), true);}
            case MARKET_ORDER_SELL -> {placeMarketOrder(event.data(), false);}
            case CREATE_ACCOUNT -> {createAccountFromJSON(event.data());}
            case LATEST_STOCK_QUOTE -> {scheduleStockQuoteRetrieval(event.data());}
            case TASK_CANCEL -> {cancelTask(event.data());}
            case DEMO_APP -> {startDemoMode();}
        }
    }

    @Override
    public void startUpSubscribedEvents() {
        eventChannel.subscribeToEvent(this, AppEventType.MARKET_ORDER_BUY);
        eventChannel.subscribeToEvent(this, AppEventType.MARKET_ORDER_SELL);
        eventChannel.subscribeToEvent(this, AppEventType.CREATE_ACCOUNT);
        eventChannel.subscribeToEvent(this, AppEventType.LATEST_STOCK_QUOTE);
        eventChannel.subscribeToEvent(this, AppEventType.TASK_CANCEL);
        eventChannel.subscribeToEvent(this, AppEventType.DEMO_APP);
    }

    public void placeMarketOrder(Object targetStock, boolean isBuy) {
        Triple<String, String, OrderType> orderData;
        if (!(targetStock instanceof Triple<?,?,?>)) {
            log.error("Bad data casting. Object is required to be Triple<String, String, OrderType>");
            return;
        }
        orderData = (Triple<String, String, OrderType>) targetStock;
        activeAccount.tradingApi.placeMarketOrder(orderData.getLeft(),
                orderData.getMiddle(), orderData.getRight(), isBuy);
    }

    public void stop() {
        dataRequester.shutdown();
        try {
            if (demoMode) accounts.remove(activeAccount);
            apiStore.saveAPIsToFile(accounts);
        } catch (IOException e) {
            log.error("Could not save api data to file", e);
        }
    }

    public ObservableList<Account> getAccounts() {return accounts;}
}
