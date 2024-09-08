package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserInterface extends Application {
    private Stage window;
    private Logger log;

    public UserInterface(){
        log = LogManager.getLogger("ui");
    }

    @Override
    public void start(Stage stage) throws Exception {
        window = stage;
        Parent root = FXMLLoader.load(getClass().getResource("/application.fxml"));
        stage.setTitle("Hello World");
        stage.setScene(new Scene(root));
        window.setOnCloseRequest(windowEvent -> {
            log.info("Stage is closing");
            // Cleanup
            Platform.exit();
        });

        stage.show();
    }

    public void Launch(String config[]){
        launch(config);
    }

    @Override
    public void stop(){
        log.info("Application is stopping");
        // Cleanup
    }
}
