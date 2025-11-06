package broker;

/**
 * Data class to help organise data needed to communicate to the API for the Account.
 * @param tradingAPI The trading API that the account is linked to.
 * @param type If the account is a demo or live one.
 * @param key The secret API key for authentication.
 * @param keyID Optional public key for authentication.
 */
public record ApiData(TradingAPI tradingAPI, AccountType type, String key, String keyID) {
}
