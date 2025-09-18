package ui;

import Data.Instrument;
import Data.Position;
import broker.Account;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class LandingController extends UIController {
    private static final Logger log = LoggerFactory.getLogger("ui");
    @FXML private MenuBar fx_titleMenu;
    @FXML private TableView<Account> fx_accounts;
    @FXML private ListView<MenuButton> fx_tickers;
    @FXML private TableView<Position> fx_openOrders;
    @FXML private TableView<Position> fx_closedOrders;
    @FXML private TabPane fx_chartsTabPane;
    private Stage mainStage;

    public LandingController(FXLoading fxLoader) {
        super(fxLoader);
    }

    @Override
    public void loadCSS(Scene scene) {
        var styles = scene.getStylesheets();
        //styles.add(getClass().getResource("/debug.css").toExternalForm());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableOrders();
        setupTableAccounts();
    }

    @FXML
    private void openConnectionMenu(ActionEvent e) {
        fxLoaderRef.LoadConnectionPage();
    }

    @FXML
    private void closeApplication(ActionEvent e) {
        mainStage = (Stage) fx_titleMenu.getScene().getWindow();
        mainStage.close();
    }

    private void setupTableOrders() {
        fx_openOrders.setPlaceholder(new Label("No rows to display"));
        var columns = fx_openOrders.getColumns();
        columns.get(0).setCellValueFactory(new PropertyValueFactory<>("ticker"));
        columns.get(1).setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
    }

    private void setupTableAccounts() {
        fx_accounts.setPlaceholder(new Label("No accounts to display."));
        var columns = fx_accounts.getColumns();
        columns.get(1).setCellValueFactory(new PropertyValueFactory<>("brokerName"));
        columns.get(3).setCellValueFactory(new PropertyValueFactory<>("freeCash"));
        columns.get(4).setCellValueFactory(new PropertyValueFactory<>("investedCash"));
        columns.get(5).setCellValueFactory(new PropertyValueFactory<>("freeCash"));
    }

    public void updateTickers(Instrument[] instruments) {

        // Populate list with new items
        if (fx_tickers.getItems().isEmpty()) {
            for (int i = 0; i < instruments.length; i++) {
                Instrument inst = instruments[i];
                MenuButton tickerButton = createTickerButton(inst);
                fx_tickers.getItems().add(tickerButton);
            }
        }
        else {
            // TODO update prices of existing button
        }
    }

    private MenuButton createTickerButton(Instrument inst) {
        // TODO change from buttons to tilted panes
        //  content is buy/sell options
        String text = inst.ticker + "     " + inst.type + "     " + inst.currencyCode;
        MenuButton ticker = new MenuButton(text);
        MenuItem buySellItem = new MenuItem("Create Order");
        buySellItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openOrderMenu(inst.ticker, 0.1f);
            }
        });

        MenuItem openChart = new MenuItem("Open Chart");
        openChart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openChart(inst.ticker);
            }
        });

        // TODO only for testing, change to replace existing values instead if they exist.
        ticker.getItems().add(buySellItem);
        ticker.getItems().add(openChart);
        return ticker;
    }

    public void openOrderMenu(String id, float quantity) {
        log.info("Sending buy order of {}:{}", quantity, id);
        fxLoaderRef.createOrderMenu();
        //fxLoaderRef.sendBuyOrder(id, quantity);
    }

    public void sellStock() {

    }

    public void updateOrders(Position[] positions) {
        // TODO get tableview from Orders tab and populate
        var list = fx_openOrders.getItems();
        list.clear();
        // Populate list with new items
        for (int i = 0; i < positions.length; i++) {
            Position position = positions[i];
            fx_openOrders.getItems().add(position);
        }
    }

    public void setAccountTableData(ObservableList<Account> accounts) {
        fx_accounts.setItems(accounts);
    }

    public void openChart(String ticker) {
        // create chart and wait for data to be updated
        log.debug("Opening chart");

        CandlestickChart chart = new CandlestickChart("[TF1D]", "Mock Data", "TF1D");

        Tab tab = new Tab(ticker);
        tab.setContent(chart);

        fx_chartsTabPane.getTabs().add(tab);
    }

    // Refresh all visible cells in every table.
    public void refreshAllTables() {
        fx_accounts.refresh();
        fx_tickers.refresh();
    }
}
