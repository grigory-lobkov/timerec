package storage.connectImpl;

import org.postgresql.ds.PGConnectionPoolDataSource;
import storage.IConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton class, returning one JDBC connection
 */
public class PgConnectionPool implements IConnectionPool {

    // JDBC driver name and database URL
    static private final String JDBC_DRIVER = "org.postgresql.Driver";
    static private final String JDBC_URL = "jdbc:postgresql://127.0.0.1:5433/timerec";
    // JDBC maximum concurrent connections
    //static private final int JDBC_MAX_POOL_SIZE = 1;
    // JDBC timeout (seconds) to acquire new connection from the pool
    static private final int JDBC_CONNECTION_TIMEOUT = 1;

    //  Database credentials
    static private final String DB_USER = "postgres";
    static private final String DB_PASSWORD = "password";

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
     */ {
        if (debugLog) System.out.println("PgConnectionPool init anonymous block");
        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);
            // Create the pool
            pool = new PGConnectionPoolDataSource();
            pool.setUrl(JDBC_URL);
            pool.setUser(DB_USER);
            pool.setPassword(DB_PASSWORD);
            pool.setConnectTimeout(JDBC_CONNECTION_TIMEOUT);
            pool.setSocketTimeout(JDBC_CONNECTION_TIMEOUT);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (debugLog) System.out.println("PgConnectionPool init anonymous block end");
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
            if (debugLog) System.out.println("PgConnectionPool.pool()");
            return pool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (debugLog) System.out.println("PgConnectionPool.pool() end");
        }
        return null;
    }
}