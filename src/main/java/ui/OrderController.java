package ui;

import Data.Quote;
import broker.OrderType;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Future;

public class OrderController extends UIController {
    private final Stage stage;
    private final BorderPane rootNode;
    private Text title;
    private TextField volumeInput;
    private ComboBox<String> execution;
    private Text buyPrice;
    private Text sellPrice;

    public OrderController(FXLoading fxLoader) {
        super(fxLoader);
        stage = new Stage();
        rootNode = buildMenuLayout();

        Scene orderScene = new Scene(rootNode, 600, 400);
        stage.setScene(orderScene);
        stage.setTitle("Create an Order");
        stage.setOnCloseRequest(windowEvent -> {
            while (stage.getUserData() == null) {
                try {
                    wait(1000L);
                } catch (InterruptedException e) {
                    break;
                }
            }
            if (stage.getUserData() == null) return;

            fxLoaderRef.cancelTask(stage.getUserData());
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    private BorderPane buildMenuLayout() {
        BorderPane borderPane = new BorderPane();
        //borderPane.getStylesheets().add(OrderController.class.getResource("/debug.css").toString());
        borderPane.setPadding(new Insets(80, 0, 80, 0));

        // Add title text
        title = new Text("Symbol: Name of asset");
        title.getStyleClass().add("text");
        title.setFont(new Font("System", 24));
        title.setTextAlignment(TextAlignment.CENTER);
        borderPane.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);

        // Add FlowPane with volume entry and execution type
        FlowPane flowPane = new FlowPane();
        flowPane.getStyleClass().add("flow");
        flowPane.setAlignment(Pos.CENTER);
        flowPane.setHgap(40);
        flowPane.setPadding(new Insets(0, 40, 0, 40));
        flowPane.prefHeight(100);
        flowPane.prefWidth(600);

        final String volumeRegex = "-?\\d*(\\.\\d{0,2})?";
        volumeInput = new TextField();
        applyInputSanitation(volumeInput, volumeRegex);
        volumeInput.setPromptText("Volume e.g. 1.00");

        execution = new ComboBox<>();
        execution.setPromptText("Market Execution");
        execution.getItems().add(OrderType.MARKET.toString());

        flowPane.getChildren().add(volumeInput);
        flowPane.getChildren().add(execution);

        borderPane.setCenter(flowPane);
        // Setup buy and sell buttons with pricing
        borderPane.setBottom(createOrderSection());

        return borderPane;
    }

    private Parent createOrderSection() {
        HBox hbox = new HBox(60);
        hbox.getStyleClass().add("hbox");
        hbox.setPrefWidth(600);
        hbox.setPrefHeight(100);
        hbox.setAlignment(Pos.CENTER);
        hbox.setFillHeight(true);

        VBox buySection = new VBox(40);
        buySection.setAlignment(Pos.CENTER);
        buySection.getStyleClass().add("vbox");
        buySection.prefWidth(100);
        buySection.setFillWidth(true);
        buyPrice = new Text("Buy price");
        buyPrice.setFont(new Font("System", 18));
        Button buyOrder = new Button("Buy order");
        buyOrder.setOnAction(this::sendBuyOrder);
        buySection.getChildren().add(buyPrice);
        buySection.getChildren().add(buyOrder);


        VBox divider = new VBox();
        divider.getStyleClass().add("vbox");
        divider.setAlignment(Pos.TOP_CENTER);
        Text slash = new Text("/");
        slash.setFont(new Font("System", 48));
        divider.getChildren().add(slash);

        VBox sellSection = new VBox(40);
        sellSection.setAlignment(Pos.CENTER);
        sellSection.getStyleClass().add("vbox");
        sellSection.prefWidth(100);
        sellSection.setFillWidth(true);
        sellPrice = new Text("Sell price");
        sellPrice.setFont(new Font("System", 18));
        Button sellOrder = new Button("Sell order");
        sellOrder.setOnAction(this::sendSellOrder);
        sellSection.getChildren().add(sellPrice);
        sellSection.getChildren().add(sellOrder);

        hbox.getChildren().add(buySection);
        hbox.getChildren().add(divider);
        hbox.getChildren().add(sellSection);

        return hbox;
    }

    public void updateTitleName(String symbol) {
        Text title = (Text) rootNode.getTop();
        title.setText("Stock: " + symbol);
        title.setUserData(symbol);
    }

    public void updatePriceData(Quote quote) {
        buyPrice.setText("Buy Price: " + quote.askPrice);
        buyPrice.setUserData(quote.askPrice);
        sellPrice.setText("Sell Price: " + quote.bidPrice);
        sellPrice.setUserData(quote.bidPrice);
    }

    public void sendBuyOrder(ActionEvent event) {
        String symbol = (String) title.getUserData();
        String quantity = volumeInput.getText();
        String selectedExecutionType = execution.getValue();

        if (selectedExecutionType == null) return;
        OrderType type = OrderType.fromString(execution.getValue());

        if (symbol == null) return;
        if (quantity.isBlank()) return;

        fxLoaderRef.postBuyOrder(symbol, quantity, type);
    }

    public void sendSellOrder(ActionEvent event) {
        String symbol = (String) title.getUserData();
        String quantity = volumeInput.getText();
        OrderType type = OrderType.fromString(execution.getValue());

        if (symbol == null) return;
        if (quantity.isBlank()) return;

        fxLoaderRef.postSellOrder(symbol, quantity, type);
    }

    public void showMenu() {
        stage.requestFocus();
        stage.show();
    }

    public boolean isVisible() {
        return stage.isShowing();
    }

    public void setUserDataStage(Object obj) {
        stage.setUserData(obj);
    }
}
