package core;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ui.FXLoading;
import utility.Settings;

public class App extends Application {
    private static final Logger logUI = LogManager.getLogger("UI");
    private Manager manager;
    private FXLoading fxLoading;

    @Override
    public void start(Stage stage) throws Exception {
        manager = new Manager();
        Settings settings = Settings.getInstance();
        fxLoading = new FXLoading();
        fxLoading.LoadLandingPage(stage);
    }

    @Override
    public void stop(){
        logUI.info("Application is stopping");
        ApiHandler.shutdown();
        // Cleanup
    }

    public static void main (String[] args){
        Logger log = LogManager.getLogger("surface");
        log.info("Starting client");
        launch(args);
    }
}
