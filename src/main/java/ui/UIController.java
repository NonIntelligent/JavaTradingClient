package ui;

import javafx.fxml.Initializable;
import javafx.scene.Scene;

public abstract class UIController implements Initializable {
    protected final FXLoading fxLoaderRef;

    public UIController (FXLoading fxLoader) {this.fxLoaderRef = fxLoader;}

    public void loadCSS(Scene scene) {}
}
