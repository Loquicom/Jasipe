package db;

import db.mapper.ResultSetMapper;

import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class Database {

    private static final Logger LOGGER = Logger.getLogger(Database.class.getName());
    private static Connection db;

    static {
        ini();
        setup();
    }

    public static void ini() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Unable to find Derby driver");
            throw new IllegalStateException("Unable to connect to the database", e);
        }
        try {
            db = DriverManager.getConnection(DatabaseProperties.get("url"));
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new IllegalStateException("Unable to connect to the database", e);
        }
    }

    public static void close() throws DatabaseException {
        if (db != null) {
            try {
                db.close();
                db = null;
            } catch (SQLException e) {
                LOGGER.warning(e.getMessage());
                throw new DatabaseException("Unable to close database connection", e);
            }
        }
    }

    public static void setup() {
        LOGGER.info("Checking the database");
        if (isSet()) {
            LOGGER.info("Database is already set");
            return;
        }
        LOGGER.info("Creating table");
        DatabaseManager md = getDatabaseManager();
        md.create().forEach(sql -> execute(sql));
        List<String> content = md.content();
        if (content != null) {
            LOGGER.info("Add content");
            content.forEach(sql -> execute(sql));
        }
    }

    public static void unset() {
        if (!isSet()) {
            return;
        }
        LOGGER.info("Dropping table");
        DatabaseManager md = getDatabaseManager();
        md.drop().forEach(sql -> execute(sql));
    }

    public static boolean query(String sql) {
        try (Statement st = db.createStatement()) {
            st.executeQuery(prepare(sql));
            return true;
        } catch (SQLException | IllegalStateException e) {
            LOGGER.warning(e.getMessage());
            return false;
        }
    }

    public static <T> Optional<T> query(String sql, ResultSetMapper<T> mapper) {
        try (Statement st = db.createStatement()) {
            ResultSet result = st.executeQuery(prepare(sql));
            T obj = mapper.map(result);
            return (obj == null) ? Optional.empty() : Optional.of(obj);
        } catch (SQLException | IllegalStateException e) {
            LOGGER.warning(e.getMessage());
            return Optional.empty();
        }
    }

    public static <T> Optional<T> query(String sql, List<Object> params, ResultSetMapper<T> mapper) {
        try (PreparedStatement st = db.prepareStatement(prepare(sql))) {
            int i = 1;
            for (Object param : params) {
                st.setObject(i++, param);
            }
            ResultSet result = st.executeQuery();
            T obj = mapper.map(result);
            return (obj == null) ? Optional.empty() : Optional.of(obj);
        } catch (SQLException | IllegalStateException e) {
            LOGGER.warning(e.getMessage());
            return Optional.empty();
        }
    }

    public static boolean execute(String sql) {
        try (Statement st = db.createStatement()) {
            int result = st.executeUpdate(prepare(sql));
            return result == 1;
        } catch (SQLException e) {
            LOGGER.warning(e.getMessage());
            return false;
        }
    }

    public static boolean execute(String sql, List<Object> params) {
        try (PreparedStatement st = db.prepareStatement(prepare(sql))) {
            int i = 1;
            for (Object param : params) {
                st.setObject(i++, param);
            }
            int result = st.executeUpdate();
            return result == 1;
        } catch (SQLException e) {
            LOGGER.warning(e.getMessage());
            return false;
        }
    }

    public static Map<String, Object> insert(String sql, List<Object> params, String[] idField) {
        Map<String, Object> ids = new HashMap<>();
        try (PreparedStatement st = db.prepareStatement(prepare(sql), idField)) {
            // Ajout parametre
            int i = 1;
            for (Object param : params) {
                st.setObject(i++, param);
            }
            // Execution requete
            int result = st.executeUpdate();
            if (result != 1) {
                return ids;
            }
            // Recuperation des ids genere
            ResultSet rs = st.getGeneratedKeys();
            rs.next();
            i = 1;
            for (String field : idField) {
                ids.put(field, rs.getObject(i));
                i++;
            }
            return ids;
        } catch (SQLException e) {
            LOGGER.warning(e.getMessage());
            return ids;
        }
    }

    public static boolean isSet() {
        return query("Select * From " + DatabaseProperties.get("verification"));
    }

    public static Connection getDb() {
        return db;
    }

    private static DatabaseManager getDatabaseManager() {
        try {
            String className = DatabaseProperties.get("manager");
            Class<?> clazz = Class.forName(className);
            DatabaseManager md = (DatabaseManager) clazz.getMethod("getInstance").invoke(null);
            return md;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to find Database Manager Class", e);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new IllegalStateException("Database Manager Class is not a child of ManagaDatabase Class or no getInstance method found", e);
        }
    }

    private static String prepare(String sqlQuery) {
        String result = sqlQuery.trim();
        result = result.charAt(result.length() - 1) == ';' ? result.substring(0, result.length() - 1) : result;
        return result;
    }

}
