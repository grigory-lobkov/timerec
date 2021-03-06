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
    static private final String JDBC_DRIVER = "org.h2.Driver";
    static private final String JDBC_URL = "jdbc:h2:~/data/test";
    // JDBC maximum concurrent connections
    static private final int JDBC_MAX_POOL_SIZE = 1;
    // JDBC timeout (seconds) to acquire new connection from the pool
    static private final int JDBC_CONNECTION_TIMEOUT = 1;

    //  Database credentials
    static private final String DB_USER = "sa";
    static private final String DB_PASSWORD = "";

    // Enable to verbose mode
    static private boolean debugLog = false;

    /**
     * The main property of this class - connection to JDBC
     */
    private JdbcConnectionPool pool = null;

    /**
     * Constructor of class
     */ {
        if (debugLog) System.out.println("H2ConnectionPool init anonymous block");
        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);
            // Create the pool
            pool = JdbcConnectionPool.create(JDBC_URL, DB_USER, DB_PASSWORD);
            pool.setMaxConnections(JDBC_MAX_POOL_SIZE);
            pool.setLoginTimeout(JDBC_CONNECTION_TIMEOUT);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (debugLog) System.out.println("H2ConnectionPool init anonymous block end");
    }

    public String fromDual(){
        return "FROM dual";
    };
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
     *
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
            if (debugLog) System.out.println("H2ConnectionPool.pool()");
            return pool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (debugLog) System.out.println("H2ConnectionPool.pool() end");
        }
        return null;
    }
}