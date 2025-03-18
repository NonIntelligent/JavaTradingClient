package core;

import Data.Instrument;
import Data.Order;
import Data.Position;
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
        manager = new Manager(this);
        Settings settings = Settings.getInstance();
        fxLoading = new FXLoading(this);
        fxLoading.LoadLandingPage(stage);
        manager.beginProcessing();

        // TODO create account class to hold api key and broker data.
        // Possibly use a builder to create the class and setup it's methods to call.

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

    public void updateInstruments(Instrument[] instruments) {
        fxLoading.showAllTickers(instruments);
    }

    public void updateOrders(Position[] positions) {
        fxLoading.showAllOrders(positions);
    }

    public void sendDataToUI() {

    }
}
