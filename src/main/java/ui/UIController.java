package ui;

import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;

import java.io.File;

/**
 * Extension class for the JavaFX Controller that can communicate with the FXML handler, {@link FXLoading}.
 */
public abstract class UIController implements Initializable {
    protected final FXLoading fxLoaderRef;

    public UIController (FXLoading fxLoader) {this.fxLoaderRef = fxLoader;}

    /**
     * Load a css file and apply to the scene.
     * @param scene The scene to apply the css style to.
     * @param fileName The CSS file to load.
     */
    public void loadCSS(Scene scene, String fileName) {
        if (!fileName.endsWith(".css")) return;

        var styles = scene.getStylesheets();
        styles.add(getClass().getResource(File.separator + fileName).toExternalForm());
    }

    /**
     * Apply input sanitation to any JavaFX object that accept user text input and is checked per character.
     * @param textInput JavaFX object to apply input limits to.
     * @param regex Regex notation that is applied on every new character entered.
     */
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
