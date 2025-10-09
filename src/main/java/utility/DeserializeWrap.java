package utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public abstract class DeserializeWrap<T> extends StdDeserializer<T> {
    protected DeserializeWrap() {
        this(null);
    }
    protected DeserializeWrap(Class<T> classObj) {
        super(classObj);
    }

    protected String obtainText(JsonNode node, String field, String defaultValue) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null) return defaultValue;
        return fieldNode.asText();
    }

    protected String obtainDate(JsonNode node, String field, String defaultValue) {
        String rawTime = obtainText(node, field, defaultValue);
        if (rawTime == null) return defaultValue;
        Instant instant = Instant.parse(rawTime);
        LocalDate date = LocalDate.ofInstant(instant, ZoneId.systemDefault());
        return date.toString();
    }
}
