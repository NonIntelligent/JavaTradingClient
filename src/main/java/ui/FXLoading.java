package ui;

import Data.Instrument;
import Data.Position;
import broker.Account;
import core.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        startUpSubscribedEvents();
        controllers = new HashMap<>(2);
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

    @Override
    public void processEvent(AppEvent event) {
        switch (event.type()) {
            case ALL_INSTRUMENTS -> showAllTickers((Instrument[]) event.data());
            case OPEN_POSITIONS -> showAllOrders((Position[]) event.data());
        }
    }

    @Override
    public void startUpSubscribedEvents() {
        eventChannel.subscribeToEvent(this, AppEventType.ALL_INSTRUMENTS);
        eventChannel.subscribeToEvent(this, AppEventType.OPEN_POSITIONS);
    }

    public void showAllTickers(Instrument[] instruments) {
        LandingController landing = (LandingController) controllers.get("Landing");
        landing.updateTickers(instruments);
    }

    public void showAllOrders(Position[] positions) {
        LandingController landing = (LandingController) controllers.get("Landing");
        landing.updateOrders(positions);
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
}
