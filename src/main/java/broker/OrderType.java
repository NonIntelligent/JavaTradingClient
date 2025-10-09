package broker;

import java.util.HashMap;
import java.util.Map;

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
