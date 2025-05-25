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
import java.util.ArrayList;
import java.util.List;

public class AccountApiStore {
    private static final Logger log = LoggerFactory.getLogger("FileIO");
    private final String apiFilePath;
    private final ObjectMapper mapper;

    public AccountApiStore(List<Account> accounts) {
        apiFilePath = Thread.currentThread().getContextClassLoader().getResource("").getPath()
                + "accounts.json";

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
        // Check if file needs to be created
        File file = new File(apiFilePath);
        boolean creation = file.createNewFile();

        // TODO maybe add id to avoid duplicate accounts

        // Iterate through each account and store APIData to list
        List<ApiData> apiDataList = new ArrayList<>(accounts.size());
        // TODO Encrypt api key using the client's password as key
        for (Account acc : accounts){
            apiDataList.add(acc.apiData);
        }

        // Write data to file
        mapper.writeValue(file, apiDataList);
    }

    public List<ApiData> loadStoredAPIs(String password) throws IOException {
        // check if file exists and if there are any entries
        File file = new File(apiFilePath);
        // TODO notes for encryption implementation
        //  ask for password on start-up if a JSON file exists and decrypt keys/create accounts
        //  do a small metadata api call to see if password/key is correct
        //  When saving, ask for password and check result against existing result in file
        // Read each entry and decrypt api keys with custom deserializer
        List<ApiData> badEntries = new ArrayList<>();
        List<ApiData> storedAPIs = mapper.readValue(file, new TypeReference<List<ApiData>>() {});

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

    class ApiDataSerializer extends StdSerializer<ApiData> {
        public ApiDataSerializer(Class<ApiData> classObj) {
            super(classObj);
        }

        @Override
        public void serialize(ApiData apiData, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            // TODO encrypt API key here
            jsonGenerator.writeStringField("key", apiData.key());
            jsonGenerator.writeStringField("broker", apiData.tradingAPI().broker.name());
            jsonGenerator.writeStringField("type", apiData.type().name());
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
            TradingAPI tradingAPI = ApiFactory.getApi(broker, type, keyResult, "");

            return new ApiData(keyResult, tradingAPI, type);
        }
    }

}
