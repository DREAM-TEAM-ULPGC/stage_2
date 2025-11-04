package com.dreamteam.search.util;

public class Config {
    public static String getEnvOrDefault(String key, String def) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? def : value;
    }
}
