package jasipe.db;

import jasipe.config.JasipeConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseConnection implements Database {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    public Connection db;
    public JasipeConfig config;

    public DatabaseConnection(Connection db, JasipeConfig config) {
        if (db == null || config == null) {
            throw new IllegalArgumentException("Connection and JasipeConfig can not be null");
        }
        this.db = db;
        this.config = config;
    }

    public boolean close() {
        try {
            db.close();
            return true;
        } catch (SQLException e) {
            LOGGER.warning(e.getMessage());
            return false;
        }
    }

    @Override
    public Connection getConnection() {
        return db;
    }

    @Override
    public JasipeConfig getConfiguration() {
        return config;
    }
}
