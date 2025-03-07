package ui;

import broker.Broker;
import broker.Instrument;
import core.ApiHandler;
import core.Manager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LandingController extends UIController {
    @FXML private MenuBar fx_titleMenu;
    @FXML private VBox fx_tickers;
    @FXML private MenuButton fx_tickerExample;
    private Stage mainStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void displayTickers(Instrument[] instruments) {
        fx_tickers.getChildren().removeFirst();
        ListView<MenuButton> tickerListButtons =  new ListView<>();
        for (int i = 0; i < instruments.length; i++) {
            Instrument inst = instruments[i];
            String text = inst.ticker + "     " + inst.type + "     " + inst.currencyCode;
            MenuButton ticker = new MenuButton(text);
            // TODO only for testing, change to replace existing values instead if they exist.
            tickerListButtons.getItems().add(ticker);
        }
        // TODO change instrument display layout to look neater.
        fx_tickers.getChildren().add(tickerListButtons);
    }

    @FXML
    private void openConnectionMenu(ActionEvent e) {
        fxLoaderRef.LoadConnectionPage();
    }

    @FXML
    private void closeApplication(ActionEvent e) {
        mainStage = (Stage) fx_titleMenu.getScene().getWindow();
        mainStage.close();
    }

}
