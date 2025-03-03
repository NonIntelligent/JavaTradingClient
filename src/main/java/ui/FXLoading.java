package ui;

import core.ApiHandler;
import core.Manager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class FXLoading {
    private static final Logger logUI = LogManager.getLogger("UI");

    Stage appWindow = null;

    public FXLoading() {

    }

    public void LoadLandingPage(Stage appStage, Manager manager) {
        this.appWindow = appStage;

        FXMLLoader application = new FXMLLoader(getClass().getResource("/application.fxml"));
        Parent root = null;
        try {
            root = application.load();
        } catch (IOException e) {
            logUI.error("Failed to load application.fxml", e);
        }

        UIController controller = application.getController();
        controller.setFxLoader(this);
        controller.setManager(manager);

        appWindow.setTitle("Java Market Trader");
        appWindow.setScene(new Scene(root));
        appWindow.setOnCloseRequest(windowEvent -> {
            logUI.info("Stage is closing");
            // Cleanup
            Platform.exit();
        });

        logUI.info("UI Launched");
        appWindow.show();
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

        Stage connection = new Stage();
        connection.setTitle("Connect to broker");
        Scene scene = new Scene(root);
        connection.setScene(scene);

        connection.show();
    }

    public void showAllTickers() {

    }

}
