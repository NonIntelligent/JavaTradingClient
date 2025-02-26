package ui;

import javafx.fxml.Initializable;

public abstract class UIController implements Initializable {
    protected FXLoading fxLoaderRef = null;

    public void setFxLoader(FXLoading fxLoader){
        this.fxLoaderRef = fxLoader;
    }
}
