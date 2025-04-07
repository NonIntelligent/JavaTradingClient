package ui;

import Data.Instrument;
import Data.Position;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.apache.hc.core5.util.Args;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.time.ohlc.OHLC;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.jfree.data.xy.OHLCDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.TimeZone;

public class LandingController extends UIController {
    private static final Logger log = LoggerFactory.getLogger("ui");
    @FXML private MenuBar fx_titleMenu;
    @FXML private ListView<MenuButton> fx_tickers;
    @FXML private MenuButton fx_tickerExample;
    @FXML private TableView<Position> fx_openOrders;
    @FXML private TableView<Position> fx_closedOrders;
    @FXML private TabPane fx_chartsTabPane;
    private Stage mainStage;

    public LandingController(FXLoading fxLoader) {
        super(fxLoader);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fx_openOrders.setPlaceholder(new Label("No rows to display"));
        var columns = fx_openOrders.getColumns();
        columns.get(0).setCellValueFactory(new PropertyValueFactory<>("ticker"));
        columns.get(1).setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
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

    public void updateTickers(Instrument[] instruments) {

        // Populate list with new items
        // TODO change from buttons to tilted panes
        //  content is buy/sell options
        if (fx_tickers.getItems().isEmpty()) {
            for (int i = 0; i < instruments.length; i++) {
                Instrument inst = instruments[i];
                String text = inst.ticker + "     " + inst.type + "     " + inst.currencyCode;
                MenuButton ticker = new MenuButton(text);
                MenuItem buySellItem = new MenuItem("Create Order");
                buySellItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        buyStock(inst.ticker, 0.1f);
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
                fx_tickers.getItems().add(ticker);
            }
        }
        else {

        }
    }

    public void buyStock(String id, float quantity) {
        log.info("Sending buy order of {}:{}", quantity, id);
        fxLoaderRef.sendBuyOrder(id, quantity);
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

    public void openChart(String ticker) {
        // create chart and wait for data to be updated
        // TODO create renderer on initialise?
        OHLCDataItem[] data = new OHLCDataItem[5];

        // Mock data
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        for (int i = 0; i < data.length; i++){
            calendar.setTime(Date.from(Instant.now()));
            calendar.add(Calendar.MINUTE, -i);

            data[i] = new OHLCDataItem(calendar.getTime(),
                    100.0 + 2*i, 100.0 + 5*i,
                    90.0 - 5*i, 90.0 - 2*i,
                    100.0);
        }

        DefaultOHLCDataset dataset = new DefaultOHLCDataset("ticker", data);

        JFreeChart chart = ChartFactory.createCandlestickChart(
                "Candlestick Chart","Time", "Price", dataset, false);

        // Customize plot
        XYPlot plot = (XYPlot) chart.getPlot();
        CandlestickRenderer renderer = (CandlestickRenderer) plot.getRenderer();
        renderer.setUseOutlinePaint(true);
        renderer.setUpPaint(Color.GREEN);
        renderer.setDownPaint(Color.RED);

        // Create chart panel
        ChartViewer viewer = new ChartViewer(chart, true);

        Tab tab = new Tab(ticker);
        tab.setContent(viewer);

        fx_chartsTabPane.getTabs().add(tab);
    }
}
