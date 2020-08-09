package jasipe.db;

import jasipe.config.JasipeConfig;

import java.sql.Connection;

public interface Database {

    public boolean close();

    public Connection getConnection();

    public JasipeConfig getConfiguration();

}
