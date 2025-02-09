package ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class FXManager {
    private final Logger logUI = LogManager.getLogger("UI");
    HashSet<FXMLLoader> loaders = new HashSet<>();
    HashMap<FXMLLoader, UIController> loaders2;

    Stage appWindow;

    public FXManager(Stage appStage) throws IOException {
        appWindow = appStage;
        FXMLLoader application = new FXMLLoader(getClass().getResource("/application.fxml"));
        Parent root = application.load();

        appWindow.setTitle("Java Market Trader");
        appWindow.setScene(new Scene(root));
        appWindow.setOnCloseRequest(windowEvent -> {
            logUI.info("Stage is closing");
            // Cleanup
            Platform.exit();
        });

        loaders.add(application);

        logUI.info("UI Launched");
        appWindow.show();
    }

    public void LoadConnectionPage(){
        // TODO check if loaded (add to list if not)

        // TODO Load connection and assign API handler
    }
}
