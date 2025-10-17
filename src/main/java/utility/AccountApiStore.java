package utility;

import broker.*;
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

public class AccountApiStore {
    private static final Logger log = LoggerFactory.getLogger("FileIO");
    private final String apiFilePath;
    private final File apiCache;
    private final ObjectMapper mapper;

    public AccountApiStore(List<Account> accounts) {
        String filePathTemp = "";

        String absolutePath = new File(filePathTemp).getAbsolutePath();
        log.debug("Empty string path {}", absolutePath);

        try {
            filePathTemp = AccountApiStore.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            log.debug("Cache file location using Protection Domain is {}", filePathTemp);
            File tempFile = new File(filePathTemp);
            if (filePathTemp.endsWith(".jar")) {
                tempFile = tempFile.getParentFile();
            }
            filePathTemp = tempFile.getAbsolutePath() + File.separator + "accounts.json";
            log.debug("Cache file location using absolute is {}", filePathTemp);
        } catch (URISyntaxException e) {
            log.error("Error in creating file due to URI issue when getting path.", e);
        }

        apiFilePath = filePathTemp;

        apiCache = new File(apiFilePath);
        log.debug("Cache file will be located at {}", apiFilePath);

        mapper = new ObjectMapper();
        // Create and register custom serialize modules
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

    // Save APIData class into a JSON file
    public void saveAPIsToFile(List<Account> accounts) throws IOException {
        if (!apiCache.exists()){
            boolean created = apiCache.createNewFile();
            log.info("Creating new API cache file (accounts.json).");
        }

        if (accounts.isEmpty()) {
            log.info("Skipping save process. No accounts were loaded.");
            return;
        }

        // TODO maybe add id to avoid duplicate accounts

        // Iterate through each account and store APIData to list
        List<ApiData> apiDataList = new ArrayList<>(accounts.size());
        // TODO Encrypt api key using the client's password as key
        for (Account acc : accounts){
            apiDataList.add(acc.getApiData());
        }
        log.info("Saving account data to file");
        // Write data to file
        mapper.writeValue(apiCache, apiDataList);
    }

    public List<ApiData> loadStoredAPIs(String password) throws IOException {
        List<ApiData> storedAPIs = new ArrayList<>();

        // TODO notes for encryption implementation
        //  ask for password on start-up if a JSON file exists and decrypt keys/create accounts
        //  do a small metadata api call to see if password/key is correct
        //  When saving, ask for password and check result against existing result in file

        if (!apiCache.exists()) return storedAPIs;
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

    // Deletes the JSON file containing the API information
    public void removeAllAccounts(){}

    public boolean checkCacheExists() {
        return apiCache.exists();
    }

    class ApiDataSerializer extends StdSerializer<ApiData> {
        public ApiDataSerializer(Class<ApiData> classObj) {
            super(classObj);
        }

        @Override
        public void serialize(ApiData apiData, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            // TODO encrypt API key here
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
