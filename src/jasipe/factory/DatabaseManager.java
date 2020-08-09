package jasipe.factory;

import jasipe.config.JasipeConfig;
import jasipe.config.JasipeProperties;
import jasipe.db.Database;
import jasipe.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static Database database = null;

    /* --- Create Connection --- */

    public static Database connection(String jdbcURL) {
        return connection(jdbcURL, JasipeProperties.loadConfig());
    }

    public static Database otherConnection(String jdbcURL) {
        return otherConnection(jdbcURL, JasipeProperties.loadConfig());
    }

    public static Database connection(String jdbcURL, JasipeConfig config) {
        config.setJdbcURL(jdbcURL);
        return connection(config);
    }

    public static Database otherConnection(String jdbcURL, JasipeConfig config) {
        config.setJdbcURL(jdbcURL);
        return otherConnection(config);
    }

    public static Database connection(JasipeConfig config) {
        // Check if database is already connected
        if (database != null) {
            throw new IllegalStateException("Database is already connected, please close connection before opening a new one");
        }
        // Connect to the database
        database = databaseConnection(config);
        return database;
    }

    public static Database otherConnection(JasipeConfig config) {
        // Connect to the database
        return databaseConnection(config);
    }

    private static Database databaseConnection(JasipeConfig config) {
        // If they are no jdbc url in config
        if (config.getJdbcURL() == null) {
            throw new IllegalArgumentException("No JDBC URL found");
        }
        // Connect to database
        try {
            Connection connection = DriverManager.getConnection(config.getJdbcURL());
            return new DatabaseConnection(connection, config);
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new IllegalStateException("Unable to connect to the database", e);
        }
    }

    /* --- Close Connection --- */

    public static void close() {
        if (database != null) {
            databaseClose(database);
        }
    }

    public static void otherClose(Database otherDb) {
        databaseClose(otherDb);
    }

    private static void databaseClose(Database db) {
        db.close();
    }

    /* --- Connection Helper --- */

    public static Database sqliteConnection(String dbName) {
        return sqliteConnection(dbName, JasipeProperties.loadConfig());
    }

    public static Database sqliteConnection(String dbName, JasipeConfig config) {
        return connection("jdbc:sqlite:" + dbName, config);
    }

    /* --- Other Helper --- */

    /**
     * Get database object
     * If the database object is not created, an attempt is made to create it.
     *
     * @return The main Database
     */
    public static Database getDatabase() {
        if (database == null) {
            connection(JasipeProperties.loadConfig());
        }
        return database;
    }

}
