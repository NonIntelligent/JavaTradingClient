package Data;

public class Order {
    public String creationTime;
    public long id;
    public String ticker;
    public String status;
    public Float value;

    public String getTicker() {
        return ticker;
    }

    public Float getValue() {
        return value;
    }
}
