package ui;

import Data.Instrument;
import Data.Order;
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
    @FXML private TableView<Position> fx_positions;
    @FXML private TableView<Order> fx_orders;
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
        fx_tickers.setCellFactory(lv -> new ListCell<MenuButton>() {
            @Override
            protected void updateItem(MenuButton item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    item.prefWidthProperty().bind(lv.widthProperty().subtract(28));
                    item.setMaxWidth(Control.USE_PREF_SIZE);
                    setGraphic(item);
                    setTooltip(item.getTooltip());
                } else {
                    setGraphic(null);
                    setTooltip(null);
                }
            }
        });
        setupTablePositions();
        setupTableOrders();
        setupTableAccounts();
    }

    @FXML
    private void openConnectionMenu(ActionEvent e) {
        fxLoaderRef.LoadConnectionPage();
    }

    @FXML
    private void demoApplication(ActionEvent e) {
        // TODO clear all table data and lists
        fxLoaderRef.informManagerToSetupDemo();
    }

    @FXML
    private void closeApplication(ActionEvent e) {
        mainStage = (Stage) fx_titleMenu.getScene().getWindow();
        mainStage.close();
    }

    private void setupTablePositions() {
        fx_positions.setPlaceholder(new Label("No open positions"));
        var columns = fx_positions.getColumns();
        columns.get(0).setCellValueFactory(new PropertyValueFactory<>("symbol"));
        columns.get(1).setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        columns.get(2).setCellValueFactory(new PropertyValueFactory<>("quantity"));
        columns.get(3).setCellValueFactory(new PropertyValueFactory<>("avgEntry"));
        columns.get(4).setCellValueFactory(new PropertyValueFactory<>("profitLoss"));
    }

    private void setupTableOrders() {
        fx_orders.setPlaceholder(new Label("No recent orders"));
        var columns = fx_orders.getColumns();
        columns.get(0).setCellValueFactory(new PropertyValueFactory<>("symbol"));
        columns.get(1).setCellValueFactory(new PropertyValueFactory<>("creationTime"));
        columns.get(2).setCellValueFactory(new PropertyValueFactory<>("executionType"));
        columns.get(3).setCellValueFactory(new PropertyValueFactory<>("side"));
        columns.get(4).setCellValueFactory(new PropertyValueFactory<>("quantity"));
        columns.get(5).setCellValueFactory(new PropertyValueFactory<>("status"));
        columns.get(6).setCellValueFactory(new PropertyValueFactory<>("filledValue"));
        columns.get(7).setCellValueFactory(new PropertyValueFactory<>("filledTime"));
    }

    private void setupTableAccounts() {
        fx_accounts.setPlaceholder(new Label("No accounts to display."));
        var columns = fx_accounts.getColumns();
        columns.get(0).setCellValueFactory(new PropertyValueFactory<>("accountID"));
        columns.get(1).setCellValueFactory(new PropertyValueFactory<>("brokerName"));
        columns.get(2).setCellValueFactory(new PropertyValueFactory<>("currencyCode"));
        columns.get(3).setCellValueFactory(new PropertyValueFactory<>("totalCash"));
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
        String text = inst.symbol;
        MenuButton ticker = new MenuButton(text);
        if (inst.name == null) {
            boolean nullable = true;
            log.error("Instrument name is null");
        }
        ticker.setTooltip(new Tooltip(inst.name));
        ticker.setUserData(inst);
        MenuItem buySellItem = new MenuItem("Create Order");
        buySellItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openOrderMenu(ticker);
            }
        });

        MenuItem openChart = new MenuItem("Open Chart");
        openChart.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                openChart(inst.symbol);
            }
        });

        // TODO only for testing, change to replace existing values instead if they exist.
        ticker.getItems().add(buySellItem);
        ticker.getItems().add(openChart);
        return ticker;
    }

    public void openOrderMenu(MenuButton ticker) {
        log.info("Creating order menu");
        fxLoaderRef.createOrderMenu((Instrument) ticker.getUserData());
    }

    public void updateOrders(Order[] orders) {
        var list = fx_orders.getItems();
        list.clear();
        // Populate list with new items
        for (int i = 0; i < orders.length; i++) {
            Order order = orders[i];
            fx_orders.getItems().add(order);
        }
    }

    public void sellStock() {

    }

    public void updatePositions(Position[] positions) {
        var list = fx_positions.getItems();
        list.clear();
        // Populate list with new items
        for (int i = 0; i < positions.length; i++) {
            Position position = positions[i];
            fx_positions.getItems().add(position);
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
