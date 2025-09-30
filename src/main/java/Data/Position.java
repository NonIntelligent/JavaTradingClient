package Data;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import utility.DeserializeWrap;

import java.io.IOException;

@JsonDeserialize(using = PositionDeserializer.class)
public class Position {
    public String symbol;
    public String currentPrice;
    public String quantity;
    public String marketValue;
    public String avgEntry;
    public String profitLoss;

    public String getSymbol() {return symbol;}
    public String getCurrentPrice() {return currentPrice;}
    public String getQuantity() {return quantity;}
    public String getMarketValue() {return marketValue;}
    public String getAvgEntry() {return avgEntry;}
    public String getProfitLoss() {return profitLoss;}
}

class PositionDeserializer extends DeserializeWrap<Position> {
    public PositionDeserializer(){this(null);}
    public PositionDeserializer(Class<Position> classObj) {
        super(classObj);
    }

    @Override
    public Position deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);

        // Create and populate Position data from json string
        Position pos = new Position();

        pos.quantity = node.get("qty").asText();
        pos.currentPrice = node.get("current_price").asText();
        pos.marketValue = node.get("market_value").asText();
        pos.avgEntry = node.get("avg_entry_price").asText();
        pos.profitLoss = node.get("unrealized_pl").asText();

        JsonNode symbolNode = node.get("symbol");
        if (symbolNode == null) symbolNode = node.get("ticker");
        pos.symbol = symbolNode.asText();

        return pos;
    }
}
