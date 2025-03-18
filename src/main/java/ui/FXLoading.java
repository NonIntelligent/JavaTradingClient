package ui;

import Data.Instrument;
import Data.Order;
import Data.Position;
import core.App;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;

public class FXLoading {
    private static final Logger logUI = LogManager.getLogger("UI");
    private final App app;
    private Stage mainWindow = null;
    private HashMap<String, UIController> controllers;

    public FXLoading(App app) {
        this.app = app;
        controllers = new HashMap<>(2);
    }

    public void LoadLandingPage(Stage appStage) {
        this.mainWindow = appStage;

        FXMLLoader application = new FXMLLoader(getClass().getResource("/application.fxml"));
        Parent root = null;
        try {
            root = application.load();
        } catch (IOException e) {
            logUI.error("Failed to load application.fxml", e);
        }

        UIController controller = application.getController();
        controller.setFxLoader(this);
        controllers.put("Landing", controller);

        mainWindow.setTitle("Java Market Trader");
        mainWindow.setScene(new Scene(root));
        mainWindow.setOnCloseRequest(windowEvent -> {
            logUI.info("Stage is closing");
            // Cleanup
            Platform.exit();
        });

        logUI.info("UI Launched");
        mainWindow.show();
    }

    public void LoadConnectionPage() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/connection.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            logUI.error("Failed to load connection.fxml.", e);
            return;
        }

        UIController controller = loader.getController();
        controller.setFxLoader(this);
        controllers.put("Connection", controller);

        Stage connection = new Stage();
        connection.setTitle("Connect to broker");
        Scene scene = new Scene(root);
        connection.setScene(scene);

        connection.setOnCloseRequest(windowEvent -> {
            logUI.info("Connection stage is closing");
            controllers.remove("Connection");
        });

        connection.show();
    }

    public void showAllTickers(Instrument[] instruments) {
        LandingController landing = (LandingController) controllers.get("Landing");
        landing.updateTickers(instruments);
    }

    public void showAllOrders(Position[] positions) {
        LandingController landing = (LandingController) controllers.get("Landing");
        landing.updateOrders(positions);
    }

}
