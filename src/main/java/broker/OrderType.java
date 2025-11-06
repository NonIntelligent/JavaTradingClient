package broker;

import java.util.HashMap;
import java.util.Map;

/**
 * Different ways to execute an Order for trades.
 */
public enum OrderType {
    MARKET("Market Order"),
    LIMIT("Limit Order"),
    STOP("Stop Order"),
    STOP_LIMIT("Stop_Limit Order");

    private final String prettyName;

    public static Map<String, OrderType> nameMap;

    OrderType(String prettyName) {
        this.prettyName = prettyName;
    }

    static {
        nameMap = new HashMap<>();
        for (OrderType type : OrderType.values()) {
            nameMap.put(type.prettyName, type);
        }
    }

    @Override
    public String toString() {
        return prettyName;
    }

    /**
     * Find an OrderType from the given String.
     * @param prettyName The {@link #toString()} value of an {@code OrderType}.
     * @return The order execution type otherwise {@code null}.
     */
    public static OrderType fromString(String prettyName) {
        if (prettyName == null) {
            throw new IllegalArgumentException("Provided String is null");
        }

        OrderType result = nameMap.get(prettyName);
        if (result == null) {
            throw new IllegalArgumentException("Provided String does not match any orderType. " + prettyName);
        }

        return result;
    }
}
