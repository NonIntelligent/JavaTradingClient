package broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The user account that is linked to a specified Trading API.
 * Store and keep track of api data, cash, and other metadata.
 */
public class Account {
    private static final Logger log = LoggerFactory.getLogger(Account.class);

    public final TradingAPI tradingApi;
    public final AccountType accountType;
    public final String apiKey;
    public final String apiKeyID;

    public String accountID;
    public String currencyCode;
    public double freeCash = 100;
    public double investedCash = 200;
    public double totalCash = freeCash + investedCash;

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
    public String getAccountID(){return accountID;}
    public String getCurrencyCode(){return currencyCode;}
    public String getBrokerName(){return tradingApi.broker.name;}
    public Double getFreeCash(){return freeCash;}
    public Double getInvestedCash(){return investedCash;}
    public Double getTotalCash(){return totalCash;}
    public ApiData getApiData() {return new ApiData(tradingApi, accountType, apiKey, apiKeyID);}
}

