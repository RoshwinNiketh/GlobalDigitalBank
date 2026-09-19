package com.gdb.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class AccountRulesPropertiesLoader {
    private final Properties properties = new Properties();

    public AccountRulesPropertiesLoader(String configPath) {
        loadProperties(configPath);
    }

    private void loadProperties(String configPath) {
        InputStream input = null;

        try {
            input = getClass()
                    .getClassLoader()
                    .getResourceAsStream(configPath);

            if (input == null) {
                File file = new File(configPath);

                if (file.exists()) {
                    input = new FileInputStream(file);
                }
            }

            if (input == null) {
                System.err.println("Warning: Could not find properties file: " + configPath);
                return;
            }

            properties.load(input);
        } catch (Exception e) {
            System.err.println(
                    "Warning: Could not load properties file "
                            + configPath + ": " + e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignored) {
                    // Nothing else can be done while closing the configuration stream.
                }
            }
        }
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public double getDouble(String key, double defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}