package jasipe.config;

import java.util.logging.Logger;

public class JasipeProperties {

    private static final Logger LOGGER = Logger.getLogger(JasipeProperties.class.getName());

    public static JasipeConfig loadConfig() {
        JasipeConfig config;
        Properties properties = new Properties();
        if (properties.open("/jasipe.properties")) {
            config = setConfig(properties);
        } else {
            config = new JasipeConfig();
            LOGGER.info("Unable to load jasipe.properties");
        }
        return config;
    }

    private static JasipeConfig setConfig(Properties properties) {
        JasipeConfig config = new JasipeConfig();
        config.setJdbcURL(properties.getStringOrDefault("url", null));
        config.setQueryTimeout(properties.getIntegerOrDefault("timeout", 0));
        config.setCacheEnabled(properties.getBooleanOrDefault("cache", true));
        config.setCheckTable(properties.getBooleanOrDefault("check", true));
        config.setCreateTable(properties.getBooleanOrDefault("create", false));
        return config;
    }

}
