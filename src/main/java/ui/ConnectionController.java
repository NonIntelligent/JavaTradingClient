package ui;

import broker.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

// Handles UI request for connecting to the API and disconnecting from the session
public class ConnectionController extends UIController {
    @FXML
    ComboBox<String> fx_brokerApiSelection;
    @FXML
    TextField fx_apiKey;
    @FXML
    TextField fx_apiID;
    @FXML
    RadioButton fx_accTypeDemo;
    @FXML
    Button fx_connectButton;
    @FXML
    Text fx_status;

    public ConnectionController(FXLoading fxLoader) {
        super(fxLoader);
    }

    @Override
    public void loadCSS(Scene scene) {

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO - Store API token securely and auto reconnect
        for (Broker b : Broker.values()) {
            fx_brokerApiSelection.getItems().add(b.name);
        }

        final String regexSanitationKey = "[a-zA-Z0-9]*";

        applyInputSanitation(fx_apiKey, regexSanitationKey);
        applyInputSanitation(fx_apiID, regexSanitationKey);
    }

    @FXML
    /*
    * Purely fetches the data entered into the UI and sends it to the Manager class
    * as a JSON string.
    * */
    private void connectToApi(ActionEvent event){
        // JSON string writing setup
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode acc = mapper.createObjectNode();

        // TODO display error on screen for all inputs
        String key = fx_apiKey.getText();
        String apiID = fx_apiID.getText();
        Broker broker = Broker.get(fx_brokerApiSelection.getValue());

        if (key.isEmpty()) return;
        if (broker == null) return;

        // Retrieve account type from radio buttons
        ToggleGroup accTypeGroup = fx_accTypeDemo.getToggleGroup();
        Toggle selectedType = accTypeGroup.getSelectedToggle();
        AccountType type = selectedType == fx_accTypeDemo ? AccountType.DEMO : AccountType.LIVE;

        // Send account creation event with all the data needed
        acc.put("apiKey", key);
        acc.put("apiID", apiID);
        acc.put("broker", broker.name);
        acc.put("type", type.name());

        fxLoaderRef.createAccount(acc.toPrettyString());
    }

    private void applyInputSanitation(TextField textField, String regex) {
        textField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            // Return modified string
            if (newText.matches(regex)) {return change;}
            // Invalid input
            return null;
        }));
    }
}
