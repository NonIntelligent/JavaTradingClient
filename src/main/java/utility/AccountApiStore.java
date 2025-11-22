package utility;

import broker.ApiData;
import broker.Account;
import broker.AccountType;
import broker.Broker;
import broker.TradingAPI;
import broker.ApiFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to store the api details of the {@link Account} in a json file to load on future use.
 */
public class AccountApiStore {
    private static final Logger log = LoggerFactory.getLogger("FileIO");
    private final ObjectMapper mapper;
    private String fileName;
    private File apiCache;

    /**
     * Setup serialization and deserialization of the class {@link ApiData}.
     */
    public AccountApiStore() {
        // Default values
        fileName = "";
        apiCache = new File(fileName);

        // Create and register custom serialize modules for ApiData.
        mapper = new ObjectMapper();
        SimpleModule serializerModule = new SimpleModule("ApiDataSerializer",
                new Version(1, 0, 0, null, null, null));
        serializerModule.addSerializer(new ApiDataSerializer(ApiData.class));

        SimpleModule deserializerModule = new SimpleModule("ApiDataDeserializer",
                new Version(1, 0, 0, null, null, null));
        serializerModule.addDeserializer(ApiData.class, new ApiDataDeserializer(ApiData.class));

        mapper.registerModule(serializerModule);
        mapper.registerModule(deserializerModule);

        // Configure features of the mapper
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }

    /**
     * Locates the directory containing the Jar executable.
     */
    public File findExecutableDirectory() {
        String filePath = "";
        // Finds the directory containing the executable jar. Different result when executed in IDE vs JAR file.
        try {
            filePath = AccountApiStore.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            log.debug("Cache file location using Protection Domain is {}", filePath);
        } catch (URISyntaxException e) {
            log.error("Error in creating file due to URI issue when getting path.", e);
        }

        // Refer to the directory containing the jar if program was run as an executable.
        File parentFile = new File(filePath).getParentFile();
        if (filePath.endsWith(".jar")) {
            filePath = parentFile.getPath();
        }

        return new File(filePath);
    }

    /**
     * Creates the cache file on the system.
     * @param directory The location to store the file without separator at end.
     * @param fileName The name of the file.
     * @return If the file exists after creation attempt.
     */
    public boolean createCacheFile(File directory, String fileName) {
        // Append JSON format to filename if it's not included.
        this.fileName = fileName != null && fileName.endsWith(".json") ? fileName : fileName + ".json";

        // Create file at correct directory location.
        String apiFilePath = directory.getPath() + File.separator + this.fileName;
        apiCache = new File(apiFilePath);
        log.debug("Cache file will be located at {}", apiFilePath);

        try {
            if (!apiCache.exists()) {
                apiCache.createNewFile();
                log.info("Creating new API cache file {}.", this.fileName);
            }
        } catch (IOException e) {
            log.error("Failed to create the cache file.", e);
        }

        return apiCache.exists();
    }

    /**
     * Save the API information for each account into a json file. Stores api keys, broker and the account type.
     * @param accounts The list of accounts' API data to store.
     * @throws IOException If an I/O error occurred when creating a new file or writing to it.
     */
    public boolean saveAPIsToFile(List<Account> accounts) throws IOException {
        if (!doesCacheExist()) {
            log.info("Skipping save process. File was not created beforehand");
            return false;
        }

        if (accounts.isEmpty()) {
            log.info("Skipping save process. No accounts were connected.");
            return false;
        }

        // TODO maybe add id to avoid duplicate accounts

        // Retrieve ApiData from the account and write to the file.
        List<ApiData> apiDataList = new ArrayList<>(accounts.size());
        // TODO Encrypt api key using the client's password as key
        for (Account acc : accounts){
            apiDataList.add(acc.getApiData());
        }
        log.info("Saving account data to file");
        mapper.writeValue(apiCache, apiDataList);
        return true;
    }


    /**
     * Read in the {@link ApiData} from the json file.
     * @param password Used to decrypt the data. Was set by the user on first file save.
     * @return The list of {@code ApiData}.
     * @throws IOException If an I/O error occurred when reading from the file.
     */
    public List<ApiData> loadStoredAPIs(String password) throws IOException {
        List<ApiData> storedAPIs = new ArrayList<>();

        // Skip encryption with no password.
        if (password == null || password.isBlank()) {

        }

        // TODO notes for encryption implementation
        //  ask for password on start-up if a JSON file exists and decrypt keys/create accounts
        //  do a small metadata api call to see if password/key is correct
        //  When saving, ask for password and check result against existing result in file

        // Check if the cache contains data.
        if (!doesCacheExist() || apiCache.length() == 0) return storedAPIs;
        // Read each entry and decrypt api keys with custom deserializer
        List<ApiData> badEntries = new ArrayList<>();
        log.info("Reading from api cache file");
        storedAPIs = mapper.readValue(apiCache, new TypeReference<>() {});

        // Null check from deserialization results
        for (ApiData data : storedAPIs){
            if (data == null) badEntries.add(data);
        }

        storedAPIs.removeAll(badEntries);

        return storedAPIs;
    }

    public void removeEntryFromAPIFile() {}

    public boolean deleteCache() {
        if (!apiCache.exists()) return false;

        return apiCache.delete();
    }

    public boolean doesCacheExist() {
        return apiCache != null && apiCache.exists();
    }

    public String getFileName() { return fileName; }

    public String getApiCachePath() { return apiCache.getPath(); }

    class ApiDataSerializer extends StdSerializer<ApiData> {
        public ApiDataSerializer(Class<ApiData> classObj) {
            super(classObj);
        }

        @Override
        public void serialize(ApiData apiData, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            // TODO encrypt API key here
            // Write all of the fields to be saved in the file.
            jsonGenerator.writeStringField("broker", apiData.tradingAPI().broker.name());
            jsonGenerator.writeStringField("type", apiData.type().name());
            jsonGenerator.writeStringField("key", apiData.key());
            jsonGenerator.writeStringField("keyID", apiData.keyID());
            jsonGenerator.writeEndObject();
        }
    }

    class ApiDataDeserializer extends StdDeserializer<ApiData> {
        public ApiDataDeserializer(Class<ApiData> classObj) {
            super(classObj);
        }

        @Override
        public ApiData deserialize(JsonParser parser, DeserializationContext deserializationContext) {
            JsonNode node = null;
            try {
                node = parser.getCodec().readTree(parser);
            } catch (IOException e) {
                log.error("Json file could not be parsed", e);
                return null;
            }

            // Reject bad data
            String hashedKey = node.get("key").asText();
            if (hashedKey.isBlank()){
                log.warn("Api key field is blank and will be ignored");
                return null;
            }

            // Optional id for the secret key. Can be null or empty.
            String keyID = node.get("keyID").asText();

            String parsedText = node.get("broker").asText();
            Broker broker;
            AccountType type;
            try {
                broker = Broker.valueOf(parsedText);
                parsedText = node.get("type").asText();
                type = AccountType.valueOf(parsedText);
            } catch (IllegalArgumentException e) {
                log.warn("No constant matches {}", parsedText, e);
                return null;
            }


            // TODO Decrypt key using password
            String keyResult = hashedKey;

            // Construct TradingAPI object from information
            TradingAPI tradingAPI = ApiFactory.getApi(broker, type, keyResult, keyID);

            return new ApiData(tradingAPI, type, keyResult, keyID);
        }
    }

}
