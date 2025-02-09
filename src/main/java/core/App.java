package core;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.ConnectionController;
import ui.FXManager;

public class App extends Application {
    private final Logger logUI = LogManager.getLogger("UI");
    private Manager manager;
    private FXManager fxManager;

    @Override
    public void start(Stage stage) throws Exception {
        manager = new Manager();
        fxManager = new FXManager(stage);
    }

    @Override
    public void stop(){
        logUI.info("Application is stopping");
        // Cleanup
    }

    public static void main (String[] args){
        Logger log = LogManager.getLogger("surface");
        log.info("Starting client");
        launch(args);
    }
}
