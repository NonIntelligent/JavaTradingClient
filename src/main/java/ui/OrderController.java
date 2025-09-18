package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
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

public class OrderController extends UIController {
    private final Stage stage;

    public OrderController(FXLoading fxLoader) {
        super(fxLoader);
        stage = new Stage();
        Parent root = buildMenuLayout();

        Scene orderScene = new Scene(root, 600, 400);
        stage.setScene(orderScene);
        stage.setTitle("Buy Sell Orders");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    private Parent buildMenuLayout() {
        BorderPane borderPane = new BorderPane();
        //borderPane.getStylesheets().add(OrderController.class.getResource("/debug.css").toString());
        borderPane.setPadding(new Insets(80, 0, 80, 0));

        // Add title text
        Text title = new Text("Symbol: Name of asset");
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
        TextField volumeInput = new TextField();
        applyInputSanitation(volumeInput, volumeRegex);

        // TODO add new enum for market execution types
        ComboBox<String> execution = new ComboBox<>();
        execution.setPromptText("Market Execution");

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
        Text buyPrice = new Text("Bid price");
        buyPrice.setFont(new Font("System", 18));
        Button buyOrder = new Button("Buy order");
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
        Text sellPrice = new Text("Ask price");
        sellPrice.setFont(new Font("System", 18));
        Button sellOrder = new Button("Sell order");
        sellSection.getChildren().add(sellPrice);
        sellSection.getChildren().add(sellOrder);

        hbox.getChildren().add(buySection);
        hbox.getChildren().add(divider);
        hbox.getChildren().add(sellSection);

        return hbox;
    }

    public void showMenu() {
        stage.show();
    }
}
