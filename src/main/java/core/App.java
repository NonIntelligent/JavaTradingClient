package core;

import com.sun.javafx.runtime.VersionInfo;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui.FXLoading;

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

        eventChannel.subscribe(manager);
        eventChannel.subscribe(fxLoading);
    }

    @Override
    public void start(Stage stage) throws Exception {
        // JavaFX version
        log.info("JavaFX Version: {}", System.getProperty("javafx.version"));
        log.info("JavaFX Runtime Version: {}", System.getProperty("javafx.runtime.version"));

        log.info("Starting application and building interface");
        fxLoading.LoadLandingPage(stage);
        manager.beginProcessing();
    }

    @Override
    public void stop(){
        log.info("Application is stopping");
        // Let all currently running tasks finish
        manager.stop();
        eventChannel.shutdown();
        // Terminate/close all connections
        ApiHandler.terminate();
    }

    public static void main (String[] args){
        launch(args);
    }

}
