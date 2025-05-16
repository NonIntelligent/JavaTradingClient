package broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Account {
    private static final Logger log = LoggerFactory.getLogger(Account.class);

    // possibly convert datablock into a record class
    // As it will not change for the lifetime of the class and a new object can be created instead
    public ApiData apiData;
    public final String apiKey;
    public final TradingAPI tradingApi;
    public final AccountType accountType;

    public int freeCash;
    public int investedCash;

    public Account(String apiKey, TradingAPI tradingApi, AccountType accountType) {
        log.info("Creating account");
        this.apiKey = apiKey;
        this.tradingApi = tradingApi;
        this.accountType = accountType;
    }

}

