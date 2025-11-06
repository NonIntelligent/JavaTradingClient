package Data;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import utility.DeserializeWrap;

import java.io.IOException;

/**
 * A JSON serialized data structure that stores the bid/ask price.
 */
@JsonDeserialize(using = QuoteDeserializer.class)
public class Quote {
    public String bidPrice;
    public String askPrice;

    public String getBidPrice() {return bidPrice;}
    public String getAskPrice() {return askPrice;}
}

class QuoteDeserializer extends DeserializeWrap<Quote> {
    public QuoteDeserializer(){this(null);}
    public QuoteDeserializer(Class<Quote> classObj) {
        super(classObj);
    }

    @Override
    public Quote deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);
        JsonNode bid = node.findValue("bp");
        JsonNode ask = node.findValue("ap");
        Quote quote = new Quote();

        if (bid != null && ask != null) {
            quote.bidPrice = bid.asText();
            quote.askPrice = ask.asText();
        } else {
            quote.bidPrice = "0.00";
            quote.askPrice = "0.00";
        }

        return quote;
    }
}