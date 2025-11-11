package ui;

import Data.Instrument;
import Data.Order;
import Data.Position;
import Data.Quote;
import broker.Account;
import broker.OrderType;
import core.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.EventConsumer;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Future;

/**
 * Controls the loading of the FXML controller classes and handles communication between
 * them and the {@code Manager}. Also, can receive events from the event bus.
 */
public class FXLoading implements EventConsumer {
    private static final Logger log = LoggerFactory.getLogger("ui");
    private final EventChannel eventChannel;
    private Stage mainWindow = null;
    private HashMap<String, UIController> controllers;

    /**
     * @param eventChannel The event bus system to link to.
     */
    public FXLoading(EventChannel eventChannel) {
        this.eventChannel = eventChannel;
        controllers = new HashMap<>(4);
    }

    /**
     * Loads the main Landing page window from the FXML file.
     * @param appStage The main window supplied by the JavaFX {@code start} method.
     */
    public void LoadLandingPage(Stage appStage) {
        this.mainWindow = appStage;

        FXMLLoader application = new FXMLLoader(getClass().getResource("/application.fxml"));
        LandingController controller = new LandingController(this);
        Parent root = null;
        try {
            application.setController(controller);
            root = application.load();
        } catch (IOException e) {
            log.error("Failed to load application.fxml", e);
            Platform.exit();
        }

        controllers.put("Landing", controller);

        mainWindow.setTitle("Java Market Trader");
        mainWindow.setScene(new Scene(root));
        mainWindow.setOnCloseRequest(windowEvent -> {
            log.info("Stage is closing");
            Platform.exit();
        });

        log.info("UI Launched");
        mainWindow.show();
    }

    /**
     * Loads the menu to connect to the Trading API.
     */
    public void LoadConnectionPage() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/connection.fxml"));
        ConnectionController controller = new ConnectionController(this);
        Parent root = null;
        try {
            loader.setController(controller);
            root = loader.load();
        } catch (IOException e) {
            log.error("Failed to load connection.fxml.", e);
            return;
        }

        controllers.put("Connection", controller);

        Stage connection = new Stage();
        connection.setTitle("Connect to broker");
        Scene scene = new Scene(root);
        connection.setScene(scene);

        connection.setOnCloseRequest(windowEvent -> {
            log.info("Connection stage is closing");
            controllers.remove("Connection");
        });

        connection.show();
    }

    /**
     * Loads the menu to display the current stock price and send buy/sell orders.
     * @param instrument The chosen stock to create an order from.
     */
    public void createOrderMenu(Instrument instrument) {
        OrderController controller = (OrderController) controllers.get("Order");
        if (controller == null) {
            controller = new OrderController(this);
            controllers.put("Order", controller);
        }

        controller.updateTitleName(instrument.symbol);

        try {
            eventChannel.publish(instrument, AppEventType.LATEST_STOCK_QUOTE, this);
        } catch (InterruptedException e) {
            // TODO display pop-up error for user
            log.error("Could not publish order info event", e);
        }

        controller.showMenu();
    }

    public void informManagerToSetupDemo() {
        try {
            eventChannel.publish(AppEventType.DEMO_APP, this);
        } catch (InterruptedException e) {
            log.error("Could not send {} event due to interruption", AppEventType.DEMO_APP, e);
        }
    }

    @Override
    public void processEvent(AppEvent event) {
        if (event.data() == null) {
            log.debug("AppEvent data is NULL of type {}", event.type());
            return;
        }

        switch (event.type()) {
            case ALL_INSTRUMENTS -> showAllTickers(event.data());
            case OPEN_POSITIONS -> showAllPositions(event.data());
            case ALL_ORDERS -> showAllOrders(event.data());
            case LATEST_STOCK_QUOTE -> updateStockPrices(event.data());
            case TASK_GET -> stopGettingOrderInfoOnClose(event.data());
            case REFRESH_TABLES -> refreshAllTables();
        }
    }

    @Override
    public void startUpSubscribedEvents() {
        eventChannel.subscribeToEvent(this, AppEventType.ALL_INSTRUMENTS);
        eventChannel.subscribeToEvent(this, AppEventType.OPEN_POSITIONS);
        eventChannel.subscribeToEvent(this, AppEventType.ALL_ORDERS);
        eventChannel.subscribeToEvent(this, AppEventType.LATEST_STOCK_QUOTE);
        eventChannel.subscribeToEvent(this, AppEventType.TASK_GET);
        eventChannel.subscribeToEvent(this, AppEventType.REFRESH_TABLES);
    }

    /**
     * Display all stocks in a ListView.
     * @param instrumentArray The array of available stocks to trade from the API.
     */
    public void showAllTickers(Object instrumentArray) {
        if (instrumentArray instanceof Instrument[] instruments) {
            LandingController landing = (LandingController) getController("Landing");
            if (landing == null) return;
            landing.updateTickers(instruments);
        } else {
            log.error("Bad data casting. Given object was not Instruments[]");
        }
    }

    /**
     * Display all active positions in a table.
     * @param positionArray Array containing all active positions from the API.
     */
    public void showAllPositions(Object positionArray) {
        if (positionArray instanceof Position[] positions) {
            LandingController landing = (LandingController) getController("Landing");
            if (landing == null) return;
            landing.updatePositions(positions);
        } else {
            log.error("Bad data casting. Given object was not Position[]");
        }
    }

    /**
     * Display all open and closed orders in a table.
     * @param orderArray Array of all orders from the API.
     */
    public void showAllOrders(Object orderArray) {
        if (orderArray instanceof Order[] orders) {
            LandingController landing = (LandingController) getController("Landing");
            if (landing == null) return;
            landing.updateOrders(orders);
        } else {
            log.error("Bad data casting. Given object was not Order[]");
        }
    }

    /**
     * Sends the price information of a stock to the Order menu.
     * @param stockQuote The bid/ask price of the stock as a {@link Quote} object.
     */
    public void updateStockPrices(Object stockQuote) {
        if (stockQuote instanceof Quote quote) {
            OrderController order = (OrderController) getController("Order");
            if (order == null || !order.isVisible()) return;
            order.updatePriceData(quote);
        } else {
            log.error("Bad data casting. Given object was not Quote");
        }
    }

    /**
     * Sets the custom data for the Order menu's stage to be a task's {@code Future}.
     * @param futureObject The {@link Future} object that periodically retrieves a stock's quote.
     */
    public void stopGettingOrderInfoOnClose(Object futureObject) {
        Future<?> future = (Future<?>) futureObject;
        OrderController order = (OrderController) getController("Order");

        order.setUserDataStage(future);
    }

    /**
     * Refresh and update all tables in the {@code LandingController}.
     */
    public void refreshAllTables() {
        LandingController landing = (LandingController) getController("Landing");
        if (landing == null) return;
        landing.refreshAllTables();
    }

    /**
     * Send a market Buy event to the {@code Manager}.
     * @param id The id of the stock.
     * @param quantity The amount of stock to buy.
     * @param type The execution method of the order.
     */
    public void postBuyOrder(String id, String quantity, OrderType type) {
        try {
            eventChannel.publish(new ImmutableTriple<>(id, quantity, type),AppEventType.MARKET_ORDER_BUY, this);
        } catch (InterruptedException e) {
            log.error("Event publishing was interrupted", e);
        }
    }

    /**
     * Sends a market Sell event to the {@code Manager}.
     * @param id The id of the stock.
     * @param quantity The amount to sell.
     * @param type The execution method of the order.
     */
    public void postSellOrder(String id, String quantity, OrderType type) {
        try {
            eventChannel.publish(new ImmutableTriple<>(id, quantity, type),AppEventType.MARKET_ORDER_SELL, this);
        } catch (InterruptedException e) {
            log.error("Event publishing was interrupted", e);
        }
    }

    /**
     * Adds all account to the table.
     * @param accounts List of accounts to display.
     */
    public void addAllAccountsToTable(ObservableList<Account> accounts){
        LandingController landing = (LandingController) getController("Landing");
        landing.setAccountTableData(accounts);
    }

    /**
     * Sends an account creation event to the {@code Manager}.
     * @param jsonApiData Provided by the {@code ConnectionController} class with all the API data collected.
     */
    public void createAccount(String jsonApiData) {
        try {
            eventChannel.publish(jsonApiData, AppEventType.CREATE_ACCOUNT, this);
        } catch (InterruptedException e) {
            log.error("Event publishing was interrupted", e);
        }
    }

    /**
     * Informs the {@code Manager} to cancel a task.
     * @param future The future to cancel and was provided by the {@code Manager}
     */
    public void cancelTask(Object future) {
        try {
            eventChannel.publish(future, AppEventType.TASK_CANCEL, this);
        } catch (InterruptedException e) {
            log.error("Event publishing was interrupted", e);
        }
    }

    /**
     * Helper function to log errors when retrieving controllers.
     * @param nameKey The name that was given to the controller.
     * @return The controller with the given name. Can be {@code null}
     */
    public UIController getController(String nameKey) {
        UIController controller = controllers.get(nameKey);
        if (controller == null) log.error(nameKey + " controller has not been created." +
                " Incorrect name or not part of list.");

        return controller;
    }
}
