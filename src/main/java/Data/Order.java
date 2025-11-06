package Data;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import utility.DeserializeWrap;

import java.io.IOException;

/**
 * A JSON serialized data structure that stores the details of a given market order.
 */
@JsonDeserialize(using = OrderDeserializer.class)
public class Order {
    public String creationTime;
    public String filledTime;
    public String id;
    public String symbol;
    public String executionType;
    public String side;
    public String quantity;
    public String status;
    public String filledValue;

    public String getCreationTime() {return creationTime;}
    public String getFilledTime() {return filledTime;}
    public String getId() {return id;}
    public String getSymbol() {return symbol;}
    public String getExecutionType() {return executionType;}
    public String getSide() {return side;}
    public String getQuantity() {return quantity;}
    public String getStatus() {return status;}
    public String getFilledValue() {return filledValue;}
}

class OrderDeserializer extends DeserializeWrap<Order> {
    public OrderDeserializer(){this(null);}
    public OrderDeserializer(Class<Order> classObj) {
        super(classObj);
    }

    @Override
    public Order deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);

        // Create and populate Order data from json string
        Order order = new Order();

        order.id = obtainText(node, "id", "0");
        order.creationTime = obtainDate(node, "created_at", "0");
        order.filledTime = obtainDate(node, "filled_at", "0");
        order.executionType = node.get("type").asText();
        order.quantity = node.get("qty").asText();
        order.side = node.get("side").asText();
        order.status = node.get("status").asText();
        order.filledValue = node.get("filled_avg_price").asText("0");

        JsonNode symbolNode = node.get("symbol");
        if (symbolNode == null) symbolNode = node.get("ticker");
        order.symbol = symbolNode.asText();

        return order;
    }
}