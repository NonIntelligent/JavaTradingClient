package broker;

public class Account {

    // possibly convert datablock into a record class
    // As it will not change for the lifetime of the class and a new object can be created instead
    public final String apiKey;
    public final TradingAPI tradingApi;
    public final AccountType accountType;

    public int freeCash;
    public int investedCash;

    public Account(String apiKey, TradingAPI tradingApi, AccountType accountType) {
        this.apiKey = apiKey;
        this.tradingApi = tradingApi;
        this.accountType = accountType;
    }

}

