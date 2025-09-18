package ui;

import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;

public abstract class UIController implements Initializable {
    protected final FXLoading fxLoaderRef;

    public UIController (FXLoading fxLoader) {this.fxLoaderRef = fxLoader;}

    public void loadCSS(Scene scene) {}

    protected void applyInputSanitation(TextInputControl textInput, String regex) {
        textInput.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            // Return modified string
            if (newText.matches(regex)) {return change;}
            // Invalid input
            return null;
        }));
    }
}
