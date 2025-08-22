package broker;

public record ApiData(TradingAPI tradingAPI, AccountType type, String key, String keyID) {
}
