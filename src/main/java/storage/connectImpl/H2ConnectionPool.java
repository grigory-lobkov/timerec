package storage.connectImpl;

import org.h2.jdbcx.JdbcConnectionPool;
import storage.IConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton class, returning one JDBC connection
 */
public class H2ConnectionPool implements IConnectionPool {

    // JDBC driver name and database URL
    private String dbDriver = "org.h2.Driver";
    private String dbUrl = "jdbc:h2:~/data/test";

    // JDBC maximum concurrent connections
    private int dbMaxPoolSize = 5;
    // JDBC timeout (seconds) to acquire new connection from the pool
    private int dbConnectionTimeout = 10;

    //  Database credentials
    private String dbUser = "sa";
    private String dbPassword = "";

    // Enable to verbose mode
    static private boolean debugLog = false;

    /**
     * The main property of this class - connection to JDBC
     */
    private JdbcConnectionPool pool = null;

    /**
     * Constructor of class
     */
    public H2ConnectionPool(String dbDriver, String dbUrl, Integer dbMaxPoolSize, Integer dbConnectionTimeout, String dbUser, String dbPassword) {
        if (debugLog) System.out.println("H2ConnectionPool init");
        try {
            // Register JDBC driver
            Class.forName(dbDriver.isEmpty() ? this.dbDriver : dbDriver);
            // Create the pool
            pool = JdbcConnectionPool.create(dbUrl == null ? this.dbUrl : dbUrl, dbUser == null ? this.dbUser : dbUser, dbPassword == null ? this.dbPassword : dbPassword);
            pool.setMaxConnections(dbMaxPoolSize == null ? this.dbMaxPoolSize : dbMaxPoolSize);
            pool.setLoginTimeout(dbConnectionTimeout == null ? this.dbConnectionTimeout : dbConnectionTimeout);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (debugLog) System.out.println("H2ConnectionPool init end");
    }

    public String fromDual() {
        return "FROM dual";
    }

    public String preSeqNextval() {
        return "";
    }

    public String postSeqNextval() {
        return ".nextval";
    }

    public String preText() {
        return "";
    }

    public String postText() {
        return "";
    }

    /**
     * Close pool
     * <p>
     * Needed to close connection.
     * If not - Tomcat will not update WebServlet automatically.
     */
    public void close() {
        if (debugLog) System.out.println("H2ConnectionPool.close()");
        if (pool != null) {
            pool.dispose();
        }
        if (debugLog) System.out.println("H2ConnectionPool.close() end");
    }

    /**
     * Get one JDBC connection
     *
     * @return connection class
     */
    @Override
    public Connection connection() {
        try {
            if (debugLog) System.out.println("H2ConnectionPool.connection()");
            return pool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (debugLog) System.out.println("H2ConnectionPool.connection() end");
        }
        return null;
    }
}
