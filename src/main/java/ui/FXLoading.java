package ui;

import Data.Instrument;
import Data.Position;
import broker.Account;
import core.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Consumer;

import java.io.IOException;
import java.util.HashMap;

public class FXLoading implements Consumer {
    private static final Logger log = LoggerFactory.getLogger("ui");
    private final EventChannel eventChannel;
    private Stage mainWindow = null;
    private HashMap<String, UIController> controllers;

    public FXLoading(EventChannel eventChannel) {
        this.eventChannel = eventChannel;
        controllers = new HashMap<>(4);
    }

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
        }

        controllers.put("Landing", controller);

        mainWindow.setTitle("Java Market Trader");
        mainWindow.setScene(new Scene(root));
        mainWindow.setOnCloseRequest(windowEvent -> {
            log.info("Stage is closing");
            // Cleanup
            Platform.exit();
        });

        controller.loadCSS(mainWindow.getScene());

        log.info("UI Launched");
        mainWindow.show();
    }

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

    public void createOrderMenu() {
        OrderController controller = (OrderController) controllers.get("Order");
        if (controller == null) {
            controller = new OrderController(this);
            controllers.put("Order", controller);
        }

        controller.showMenu();
    }

    @Override
    public void processEvent(AppEvent event) {
        if (event.data() == null) {
            log.error("AppEvent possesses null object as data");
            return;
        }
        switch (event.type()) {
            case ALL_INSTRUMENTS -> showAllTickers(event.data());
            case OPEN_POSITIONS -> showAllOrders(event.data());
        }
    }

    @Override
    public void startUpSubscribedEvents() {
        eventChannel.subscribeToEvent(this, AppEventType.ALL_INSTRUMENTS);
        eventChannel.subscribeToEvent(this, AppEventType.OPEN_POSITIONS);
    }

    public void showAllTickers(Object instrumentArray) {
        if (instrumentArray instanceof Instrument[] instruments) {
            LandingController landing = (LandingController) getController("Landing");
            if (landing == null) return;
            landing.updateTickers(instruments);
        } else {
            log.error("Bad data casting. Given object was not Instruments[]");
        }
    }

    public void showAllOrders(Object positionArray) {
        if (positionArray instanceof Position[] positions) {
            LandingController landing = (LandingController) getController("Landing");
            if (landing == null) return;
            landing.updateOrders(positions);
        } else {
            log.error("Bad data casting. Given object was not Position[]");
        }
    }

    public void sendBuyOrder(String id, float quantity) {
        try {
            eventChannel.publish(new Pair<>(id, quantity),AppEventType.MARKET_ORDER);
        } catch (InterruptedException e) {
            log.error("Event publishing was interrupted", e);
        }
    }

    public void addAllAccountsToTable(ObservableList<Account> accounts){
        LandingController landing = (LandingController) controllers.get("Landing");
        landing.setAccountTableData(accounts);
    }

    public void createAccount(String jsonApiData) {
        try {
            eventChannel.publish(jsonApiData, AppEventType.CREATE_ACCOUNT);
        } catch (InterruptedException e) {
            log.error("Event publishing was interrupted", e);
        }
    }

    public UIController getController(String nameKey) {
        UIController controller = controllers.get(nameKey);
        if (controller == null) log.error(nameKey + " controller has not been created." +
                " Incorrect name or not part of list.");

        return controller;
    }
}
