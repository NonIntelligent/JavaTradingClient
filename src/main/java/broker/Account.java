package broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Account {
    private static final Logger log = LoggerFactory.getLogger(Account.class);

    public final TradingAPI tradingApi;
    public final AccountType accountType;
    public final String apiKey;
    public final String apiKeyID;

    public String accountID;
    public String currencyCode;
    public int freeCash;
    public int investedCash;
    public int totalCash;

    public Account(TradingAPI tradingApi, AccountType accountType, String apiKey, String apiKeyID) {
        log.info("Creating account");
        this.tradingApi = tradingApi;
        this.accountType = accountType;
        this.apiKey = apiKey;
        this.apiKeyID = apiKeyID;
    }

    public Account(ApiData apiData){
        this(apiData.tradingAPI(), apiData.type(), apiData.key(), apiData.keyID());
    }

    // Getters for tableview entries
    public String getBrokerName(){return tradingApi.broker.name;}
    public Integer getFreeCash(){return freeCash;}
    public Integer getInvestedCash(){return investedCash;}
    public Integer getTotalCash(){return totalCash;}
    public ApiData getApiData() {return new ApiData(tradingApi, accountType, apiKey, apiKeyID);}
}

