package jasipe.config;

import java.io.IOException;
import java.io.InputStream;

public class Properties {

    private static final String DEFAULT_STRING = "";
    private static final boolean DEFAULT_BOOLEAN = false;
    private static final int DEFAULT_INTEGER = 0;
    private static final double DEFAULT_DOUBLE = 0.;

    private java.util.Properties properties;

    public Properties() {
        this.properties = new java.util.Properties();
    }

    public Properties(String file) {
        this.properties = new java.util.Properties();
        this.open(file);
    }

    public Properties(java.util.Properties properties) {
        this.properties = properties;
    }

    public boolean open(String file) {
        try {
            InputStream is = Properties.class.getResourceAsStream(file);
            if (is == null) {
                return false;
            }
            this.properties.load(is);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    public String getString(String key) {
        return this.properties.getProperty(key);
    }

    public boolean getBoolean(String key) {
        String prop = this.properties.getProperty(key);
        return Boolean.parseBoolean(prop);
    }

    public int getInteger(String key) {
        String prop = this.properties.getProperty(key);
        return Integer.parseInt(prop);
    }

    public double getDouble(String key) {
        String prop = this.properties.getProperty(key);
        return Double.parseDouble(prop);
    }

    public String getStringOrDefault(String key) {
        return this.getStringOrDefault(key, DEFAULT_STRING);
    }

    public String getStringOrDefault(String key, String defaultValue) {
        String prop = this.properties.getProperty(key);
        if (prop == null) {
            return defaultValue;
        }
        return prop;
    }

    public boolean getBooleanOrDefault(String key) {
        return this.getBooleanOrDefault(key, DEFAULT_BOOLEAN);
    }

    public boolean getBooleanOrDefault(String key, boolean defaultValue) {
        String prop = this.properties.getProperty(key);
        if (prop == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(prop);
    }

    public int getIntegerOrDefault(String key) {
        return this.getIntegerOrDefault(key, DEFAULT_INTEGER);
    }

    public int getIntegerOrDefault(String key, int defaultValue) {
        String prop = this.properties.getProperty(key);
        if (prop == null) {
            return defaultValue;
        }
        return Integer.parseInt(prop);
    }

    public double getDoubleOrDefault(String key) {
        return this.getDoubleOrDefault(key, DEFAULT_DOUBLE);
    }

    public double getDoubleOrDefault(String key, double defaultValue) {
        String prop = this.properties.getProperty(key);
        if (prop == null) {
            return defaultValue;
        }
        return Double.parseDouble(prop);
    }

}
