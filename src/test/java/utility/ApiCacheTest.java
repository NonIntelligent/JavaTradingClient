package utility;

import broker.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApiCacheTest {
    private final String s = File.separator;
    private static AccountApiStore apiCache;
    private static File location;

    @BeforeAll
    public static void setup() {
        apiCache = new AccountApiStore();
        location = apiCache.findExecutableDirectory();
    }

    /**
     * Is the location of the executable directory correct in a test environment?
     */
    @Test
    void is_executable_directory_correct() {
        String workingDirectory = Paths.get("").toAbsolutePath() + s;
        String testClassLocation = workingDirectory + "target" + s + "classes";

        // Location is different during testing.
        String result = apiCache.findExecutableDirectory().getPath().toString();

        assertEquals(testClassLocation, result);

        System.out.println("Success! Cache located at " + result);
    }


    /**
     * Checks if an actual file was created on disk.
     * @param input Name of the cache file.
     */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "nameTest.so", "cacheExists.json"})
    void check_cache_exists_after_creation(String input) {
        // Cache was a new creation and did not already exist.
        boolean cache = apiCache.createCacheFile(location, input);
        assertTrue(cache);

        // Remove file for repeat tests.
        boolean deleted = apiCache.deleteCache();
        System.out.println("Cache deletion of file" + apiCache.getFileName() + ": " + deleted);
    }

    /**
     * Save a set of accounts to a file to check if writing was successful.
     */
    @Test
    void is_apiData_saved_to_file() {
        ArrayList<Account> accounts = new ArrayList<>(3);
        accounts.add(createAccount(Broker.ALPACA, AccountType.DEMO, 1));
        accounts.add(createAccount(Broker.ALPACA, AccountType.LIVE, 2));
        accounts.add(createAccount(Broker.DEMO, AccountType.DEMO, 3));

        boolean cache = apiCache.createCacheFile(location, "saveAPI.json");

        // Null exception means success.
        IOException faliure = null;
        try {
            apiCache.saveAPIsToFile(accounts);
        } catch (IOException e) {
            faliure = e;
        } finally {
            apiCache.deleteCache();
            assertNull(faliure);
        }
    }

    @Test
    void skip_saving_when_there_is_no_cache() {
        ArrayList<Account> accounts = new ArrayList<>(1);
        accounts.add(createAccount(Broker.ALPACA, AccountType.DEMO, 1));

        boolean saved = true;
        try {
            saved = apiCache.saveAPIsToFile(accounts);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertFalse(saved);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void skip_saving_with_no_accounts(ArrayList<Account> accounts) {
        boolean saved;
        try {
            saved = apiCache.saveAPIsToFile(accounts);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertFalse(saved);
    }

    /**
     * Check if the data stored is read correctly.
     */
    @Test
    void are_apis_correctly_loaded() {
        ArrayList<Account> accounts = new ArrayList<>(3);
        accounts.add(createAccount(Broker.ALPACA, AccountType.DEMO, 1));
        accounts.add(createAccount(Broker.ALPACA, AccountType.LIVE, 2));
        accounts.add(createAccount(Broker.DEMO, AccountType.DEMO, 3));

        boolean cache = apiCache.createCacheFile(location, "loadAPI.json");
        List<ApiData> apiData;

        try {
            apiCache.saveAPIsToFile(accounts);
            apiData = apiCache.loadStoredAPIs("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            apiCache.deleteCache();
        }

        boolean correct = true;

        for (int i = 0; i < apiData.size(); i++) {
            ApiData data = apiData.get(i);
            Account acc = accounts.get(i);

            if (data.tradingAPI().broker != acc.tradingApi.broker) correct = false;
            if (data.type() != acc.accountType) correct = false;
            if (!data.key().equals(acc.apiKey)) correct = false;
            if (!data.keyID().equals(acc.apiKeyID)) correct = false;
        }

        assertTrue(correct);
    }

    private Account createAccount(Broker broker, AccountType type, int num) {
        TradingAPI api = ApiFactory.getApi(broker, type, "KEY" + num, "ID" + num);
        return new Account(api, type, "KEY" + num, "ID" + num);
    }
}