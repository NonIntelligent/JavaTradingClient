package ui;

import core.Manager;
import javafx.fxml.Initializable;

public abstract class UIController implements Initializable {
    protected final FXLoading fxLoaderRef;

    public UIController (FXLoading fxLoader) {this.fxLoaderRef = fxLoader;}
}
