package Data;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import utility.DeserializeWrap;

import java.io.IOException;
import java.util.Objects;

@JsonDeserialize(using = InstrumentDeserializer.class)
public class Instrument {
    public String currencyCode;
    public String name;
    public String shortName;
    public String symbol;
    public String type;
    public boolean tradable;

    public String getSymbol() {return symbol;}
    public String getName() {return name;}
    public String getType() {return type;}
    public String getCurrencyCode() {return currencyCode;}
    public boolean getTradable() {return tradable;}
}

class InstrumentDeserializer extends DeserializeWrap<Instrument> {
    public InstrumentDeserializer(){this(null);}
    public InstrumentDeserializer(Class<Instrument> classObj) {
        super(classObj);
    }

    @Override
    public Instrument deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);

        // Create and populate instrument data from json string
        Instrument inst = new Instrument();
        inst.name = node.get("name").asText();

        JsonNode symbolNode = node.get("symbol");
        if (symbolNode == null) symbolNode = node.get("ticker");
        inst.symbol = symbolNode.asText();

        inst.type = node.get("class").asText();
        inst.tradable = node.get("tradable").asBoolean(false);

        return inst;
    }
}
