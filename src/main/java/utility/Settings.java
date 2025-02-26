package utility;

import java.io.*;
import java.util.Properties;

public class Settings {
    private static Settings settings;
    private final Properties prop;
    private final String propPath;

    private Settings() {
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        propPath = rootPath + "marketTrader.properties";

        prop = new Properties();
        loadPropertiesFile();
    }

    private void loadPropertiesFile() {
        try {
            boolean success = createFile(propPath);
            prop.load(new FileInputStream(propPath));
            if (success) setupDefaultProperties();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean createFile(String path) throws IOException{
        File file = new File(path);
        return file.createNewFile();
    }

    private void setupDefaultProperties() throws IOException {
        // Clear the file and reset to default values
        prop.clear();
        prop.setProperty("API-KEY1", "EMPTY");
        prop.store(new FileOutputStream(propPath),"Default Version 1.0");
    }


    public String getApiKey() {
        return prop.getProperty("API-KEY1");
    }

    public void setSetting(String key, String value) {
        prop.setProperty(key, value);
        try {
            prop.store(new FileOutputStream(propPath),"Changed setting");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Settings getInstance() {
        if (settings == null) {
            synchronized (Settings.class) {
                if (settings == null) {
                    settings = new Settings();
                }
            }
        }

        return settings;
    }
}
