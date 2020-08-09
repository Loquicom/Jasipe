package db;

import java.io.IOException;
import java.util.Properties;

public class DatabaseProperties {
	
	private static Properties properties = new Properties(); 
	static {
		try {
			properties.load(DatabaseProperties.class.getResourceAsStream("/database.properties"));
		} catch (IOException e) {
			throw new IllegalStateException("Unable to find database.properties", e);
		}
	}
	
	public static String get(String key) {
		return properties.getProperty(key);
	}
	
	public static boolean getBool(String key) {
		String prop = properties.getProperty(key);
		return "true".equals(prop.toLowerCase());
	}

}
