package main;

import jasipe.config.JasipeConfig;
import jasipe.db.Database;
import jasipe.factory.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) {
        Database database = null;
        try {
            // create a database connection
            database = DatabaseManager.sqliteConnection("jasipe.db");
            Connection db = database.getConnection();
            JasipeConfig config = database.getConfiguration();

            Statement statement = db.createStatement();
            statement.setQueryTimeout(config.getQueryTimeout());  // set timeout to 30 sec.

            statement.executeUpdate("drop table if exists person");
            statement.executeUpdate("create table person (id integer, name string)");
            statement.executeUpdate("insert into person values(1, 'leo')");
            statement.executeUpdate("insert into person values(2, 'yui')");
            ResultSet rs = statement.executeQuery("select * from person");
            while (rs.next()) {
                // read the result set
                System.out.println("name = " + rs.getString("name"));
                System.out.println("id = " + rs.getInt("id"));
            }
        } catch (SQLException e) {
            // if the error message is "out of memory",
            // it probably means no database file is found
            System.err.println(e.getMessage());
        } finally {
            if (database != null)
                database.close();
        }
    }

}
