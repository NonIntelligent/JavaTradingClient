package utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Wrapper class of the JSON deserializer from the Jackson library.
 * Provides extra functions to assist with retrieving values from nodes without error.
 * @param <T> The class to implement custom deserialization code.
 */
public abstract class DeserializeWrap<T> extends StdDeserializer<T> {
    protected DeserializeWrap() {
        this(null);
    }
    protected DeserializeWrap(Class<T> classObj) {
        super(classObj);
    }

    /**
     * Retrieves the value from the node as text. If the node is null or the field is incorrect
     * the provided String will be returned.
     * @param node The node object to check.
     * @param field The field name to get the result from.
     * @param defaultValue Result if something went wrong.
     * @return The value in from the node as text, otherwise the {@code defaultValue}
     */
    protected String obtainText(JsonNode node, String field, String defaultValue) {
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null) return defaultValue;
        return fieldNode.asText();
    }

    /**
     * Obtains the date from a dateTime string set in the UTC timezone.
     * The String must be formatted as an {@link Instant}.
     * @param node The node object to check.
     * @param field The field name to get the result from.
     * @param defaultValue Result if something went wrong.
     * @return The date given as text format, otherwise the {@code defaultValue}
     */
    protected String obtainDate(JsonNode node, String field, String defaultValue) {
        String rawTime = obtainText(node, field, defaultValue);
        if (rawTime == null) return defaultValue;
        Instant instant = Instant.parse(rawTime);
        LocalDate date = LocalDate.ofInstant(instant, ZoneId.systemDefault());
        return date.toString();
    }
}
