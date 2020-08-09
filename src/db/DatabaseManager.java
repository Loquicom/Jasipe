package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class DatabaseManager {
	
	private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
	
	protected DatabaseManager() {
		// Constructeur privé pour singelton
	}
	
	public abstract List<String> create();
	
	public abstract List<String> drop();
	
	public abstract List<String> content();
	
	public List<String> readSQLFile(String sqlFile) {
		try {
			URL ressource = DatabaseManager.class.getResource(sqlFile);
			return readSQLFile(new File(ressource.toURI()));
		} catch (URISyntaxException e) {
			LOGGER.severe("Unable to find SQL file: " + e.getMessage());
			return new ArrayList<>();
		}
	}
	
	public List<String> readSQLFile(File sqlFile) {
		if (! (sqlFile.exists() && sqlFile.canRead())) {
			LOGGER.warning("Unable to find SQL file (" + sqlFile.getPath() + ")");
			return new ArrayList<>();
		}
		// Lecture du fichier
		try {
			FileInputStream fis = new FileInputStream(sqlFile);
			byte[] data = new byte[(int) sqlFile.length()];
			fis.read(data);
			fis.close();
			String sql = new String(data, "UTF-8");
			return Arrays.asList(sql.split(";\n")).stream().filter(elt -> !elt.trim().isEmpty()).collect(Collectors.toList());
		} catch (IOException e) {
			LOGGER.severe("Unable to read SQL File: " + e.getMessage());
			return new ArrayList<>();
		}
	}

}
