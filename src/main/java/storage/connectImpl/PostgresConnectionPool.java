package storage.connectImpl;

import org.postgresql.ds.PGConnectionPoolDataSource;
import storage.IConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton class, returning one JDBC connection
 */
public class PostgresConnectionPool implements IConnectionPool {

    // JDBC driver name and database URL
    private String dbDriver = "org.postgresql.Driver";
    private String dbUrl = "jdbc:postgresql://127.0.0.1:5433/timerec";

    // JDBC maximum concurrent connections
    //private int dbMaxPoolSize = 5;
    // JDBC timeout (seconds) to acquire new connection from the pool
    private int dbConnectionTimeout = 10;

    //  Database credentials
    private String dbUser = "postgres";
    private String dbPassword = "";

    // Enable to verbose mode
    static private boolean debugLog = false;

    /**
     * The main property of this class - connection to JDBC
     */
    private PGConnectionPoolDataSource pool = null;

    public String fromDual() {
        return "";
    }

    public String preSeqNextval() {
        return "nextval('";
    }

    public String postSeqNextval() {
        return "')";
    }

    public String preText() {
        return "";
    }

    public String postText() {
        return "::text";
    }


    /**
     * Constructor of class
     */
    public PostgresConnectionPool(String dbDriver, String dbUrl, Integer dbMaxPoolSize, Integer dbConnectionTimeout, String dbUser, String dbPassword) {
        if (debugLog) System.out.println("PgConnectionPool init");
        try {
            // Register JDBC driver
            Class.forName(dbDriver == null ? this.dbDriver : dbDriver);
            // Create the pool
            pool = new PGConnectionPoolDataSource();
            pool.setUrl(dbUrl == null ? this.dbUrl : dbUrl);
            pool.setUser(dbUser == null ? this.dbUser : dbUser);
            pool.setPassword(dbPassword == null ? this.dbPassword : dbPassword);
            pool.setConnectTimeout(dbConnectionTimeout == null ? this.dbConnectionTimeout : dbConnectionTimeout);
            pool.setSocketTimeout(dbConnectionTimeout == null ? this.dbConnectionTimeout : dbConnectionTimeout);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (debugLog) System.out.println("PgConnectionPool init end");
    }

    /**
     * Close pool
     */
    public void close() {
    }

    /**
     * Get one JDBC connection
     *
     * @return connection class
     */
    @Override
    public Connection connection() {
        try {
            if (debugLog) System.out.println("PgConnectionPool.connection()");
            return pool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (debugLog) System.out.println("PgConnectionPool.connection() end");
        }
        return null;
    }
}
