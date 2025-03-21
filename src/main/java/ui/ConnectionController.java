package ui;

import broker.Broker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import utility.Settings;

import java.net.URL;
import java.util.ResourceBundle;

// Handles UI request for connecting to the API and disconnecting from the session
public class ConnectionController extends UIController {
    @FXML
    ChoiceBox<String> fx_brokerApiSelection;
    @FXML
    TextField fx_apiKey;
    @FXML
    Button fx_connectButton;
    @FXML
    Text fx_status;

    public ConnectionController(FXLoading fxLoader) {
        super(fxLoader);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO - Store API token securely and auto reconnect
        for (Broker b : Broker.values()) {
            fx_brokerApiSelection.getItems().add(b.name);
        }
    }

    @FXML
    /*
    * Sends login request to the API and stores the token
    *
    * */
    private void connectToApi(ActionEvent event){
        // TODO add checks for duplicates
        String key = Settings.getInstance().getApiKey();
        if (key != null) {
            // TODO call method in FXLoader -> Manager to create an Account and start processing
            return;
        }

        // TODO display error on screen for all inputs
        key = fx_apiKey.getText();
        String selectedBroker = fx_brokerApiSelection.getValue();
        if (key.isEmpty()) return;

        if (selectedBroker.isEmpty()) return;

        // Save key
        Settings.getInstance().setSetting("API-KEY1", key);
        Settings.getInstance().setSetting("API-BROKER1", selectedBroker);

    }

}
