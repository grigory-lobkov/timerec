package storage.connectImpl;

import org.mariadb.jdbc.MariaDbPoolDataSource;
import storage.IConnectionPool;

import javax.sql.PooledConnection;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton class, returning one JDBC connection
 */
public class MariadbConnectionPool implements IConnectionPool {

    // JDBC driver name and database URL
    private String dbDriver = "org.mariadb.jdbc.Driver";
    private String dbUrl = "jdbc:mariadb://127.0.0.1/db";

    // JDBC maximum concurrent connections
    private int dbMaxPoolSize = 5;
    // JDBC timeout (seconds) to acquire new connection from the pool
    private int dbConnectionTimeout = 10;

    //  Database credentials
    private String dbUser = "mysql";
    private String dbPassword = "";

    // Enable verbose mode
    private static final boolean debugLog = false;

    /**
     * The main property of this class - connection to JDBC
     */
    private MariaDbPoolDataSource pool = null;

    /**
     * Constructor of class
     */
    public MariadbConnectionPool(String dbDriver, String dbUrl, Integer dbMaxPoolSize, Integer dbConnectionTimeout, String dbUser, String dbPassword) {
        if (debugLog) System.out.println("MariadbConnectionPool init");
        try {
            // Register JDBC driver
            Class.forName(dbDriver == null ? this.dbDriver : dbDriver);
            // Create the pool
            pool = new MariaDbPoolDataSource();
            pool.setUrl(dbUrl == null ? this.dbUrl : dbUrl);
            pool.setUser(dbUser == null ? this.dbUser : dbUser);
            pool.setPassword(dbPassword == null ? this.dbPassword : dbPassword);
            pool.setLoginTimeout(dbConnectionTimeout == null ? this.dbConnectionTimeout : dbConnectionTimeout);
            int poolSize = dbMaxPoolSize == null ? this.dbMaxPoolSize : dbMaxPoolSize;
            pool.setMinPoolSize(poolSize > 2 ? poolSize / 2 : 1);
            pool.setMaxPoolSize(poolSize);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        if (debugLog) System.out.println("MariadbConnectionPool init end");
    }

    public String fromDual() {
        return "";
    }

    public String preSeqNextval() {
        return "nextval(";
    }

    public String postSeqNextval() {
        return ")";
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
        if (debugLog) System.out.println("MariadbConnectionPool.close()");
        if (pool != null) {
            pool.close();
        }
        if (debugLog) System.out.println("MariadbConnectionPool.close() end");
    }

    /**
     * Get one JDBC connection
     *
     * @return connection class
     */
    @Override
    public Connection connection() {
        try {
            if (debugLog) System.out.println("MariadbConnectionPool.connection()");
            return pool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (debugLog) System.out.println("MariadbConnectionPool.connection() end");
        }
        return null;
    }
}
