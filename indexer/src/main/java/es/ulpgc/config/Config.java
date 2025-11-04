package es.ulpgc.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Config {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("Configuration file not found.");
            }
            properties.load(input);
        } catch (IOException exception) {
            System.err.println("Error loading configuration file: " + exception.getMessage());
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
        try (OutputStream output = new java.io.FileOutputStream("indexer/src/main/resources/config.properties")) {
            properties.store(output, null);
        } catch (IOException exception) {
            System.err.println("Error saving configuration file: " + exception.getMessage());
        }
    }
}