package ui;

import core.ApiHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LandingController implements Initializable {
    @FXML private MenuBar fx_titleMenu;
    @FXML private VBox fx_tickers;
    private Stage mainStage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    private void openConnectionMenu(ActionEvent e) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/connection.fxml"));

        Parent root = loader.load();

        Stage connection = new Stage();
        connection.setTitle("Connect to broker");
        ConnectionController controller = loader.getController();
        ApiHandler h = new ApiHandler();
        controller.setBrokerHandler(h);
        Scene scene = new Scene(root);
        connection.setScene(scene);

        connection.show();
    }

    @FXML
    private void closeApplication(ActionEvent e) {
        mainStage = (Stage) fx_titleMenu.getScene().getWindow();
        mainStage.close();
    }


}
