package core;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.FXLoading;

/**
 * The JavaFX application class to handle the setup, startup, and shutdown processes.
 */
public class App extends Application {
    private static final Logger log = LoggerFactory.getLogger("application");
    private Manager manager;
    private FXLoading fxLoading;
    private final EventChannel eventChannel;

    public App() {
        log.info("Application is being constructed");
        eventChannel = new EventChannel();
        manager = new Manager(eventChannel);
        fxLoading = new FXLoading(eventChannel);

        eventChannel.connectToService(manager);
        eventChannel.connectToService(fxLoading);
        manager.startUpSubscribedEvents();
        fxLoading.startUpSubscribedEvents();
    }

    @Override
    public void start(Stage stage) throws Exception {
        // JavaFX version
        log.info("JavaFX Version: {}", System.getProperty("javafx.version"));
        log.info("JavaFX Runtime Version: {}", System.getProperty("javafx.runtime.version"));

        log.info("Starting application and building interface");
        fxLoading.LoadLandingPage(stage);
        // TODO present user dialog for password entry. (prompt can be ignored).
        manager.loadAccountsFromCache("");
        manager.beginProcessing();
        fxLoading.addAllAccountsToTable(manager.getAccounts());
    }

    @Override
    public void stop(){
        log.info("Application is stopping");
        // Let all currently running tasks finish
        manager.stop();
        eventChannel.shutdown();
        // Terminate/close all connections
        ApiHandler.terminate();
        System.exit(0);
    }

    public static void main (String[] args){
        launch(args);
    }

}
