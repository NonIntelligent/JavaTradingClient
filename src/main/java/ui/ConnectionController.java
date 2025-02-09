package ui;

import core.ApiHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

// Handles UI request for connecting to the API and disconnecting from the session
public class ConnectionController implements Initializable {
    @FXML
    ChoiceBox<String> fx_brokerApiSelection;
    @FXML
    TextField fx_apiKey;
    @FXML
    Button fx_connectButton;
    @FXML
    Text fx_status;

    private String[] brokers = {"Trading212"};
    private ApiHandler _apiHandler;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO - Populate broker selection
        // TODO - Store API token securely and auto reconnect
        //
        fx_brokerApiSelection.getItems().addAll(brokers);
    }

    @FXML
    /*
    * Sends login request to the API and stores the token
    *
    * */
    private void connectToApi(ActionEvent event){
        String key = fx_apiKey.getText();

        // TODO display error on screen to input API Key
        if (key.isEmpty()) return;

        if (fx_brokerApiSelection.getValue().isEmpty()) return;



        // TODO use broker handler and connect to API
        _apiHandler.connectToApi(key, fx_brokerApiSelection.getValue());
    }

    public void setBrokerHandler(ApiHandler apiHandler){
        _apiHandler = apiHandler;
    }

}
