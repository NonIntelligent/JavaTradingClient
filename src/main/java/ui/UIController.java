package ui;

import core.Manager;
import javafx.fxml.Initializable;

public abstract class UIController implements Initializable {
    protected FXLoading fxLoaderRef = null;
    protected Manager managerRef = null;

    public void setFxLoader(FXLoading fxLoader){
        this.fxLoaderRef = fxLoader;
    }

    public void setManager(Manager manager){
        this.managerRef = manager;
    }
}
