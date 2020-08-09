package jasipe.config;

public class JasipeConfig {

    private String jdbcURL = null;

    /**
     * Query timeout
     */
    private int queryTimeout = 0;

    /**
     * Enabled / Disabled cache
     */
    private boolean cacheEnabled = true;

    /**
     * On starting check if all table exist
     */
    private boolean checkTable = true;

    /**
     * If table not exist (and checkTable = true) create table
     */
    private boolean createTable = false;

    public String getJdbcURL() {
        return jdbcURL;
    }

    public void setJdbcURL(String jdbcURL) {
        this.jdbcURL = jdbcURL;
    }

    public int getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public boolean isCheckTable() {
        return checkTable;
    }

    public void setCheckTable(boolean checkTable) {
        this.checkTable = checkTable;
    }

    public boolean isCreateTable() {
        return createTable;
    }

    public void setCreateTable(boolean createTable) {
        this.createTable = createTable;
    }

}
