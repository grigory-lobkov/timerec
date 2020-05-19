package storage.jdbc.h2;

import storage.jdbc.IDBConn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class, returning one JDBC connection
 */
public class H2Conn implements IDBConn {

    // JDBC driver name and database URL
    static private final String JDBC_DRIVER = "org.h2.Driver";
    static private final String JDBC_URL = "jdbc:h2:~/data/test";

    //  Database credentials
    static private final String DB_USER = "sa";
    static private final String DB_PASSWORD = "";

    // Enable to verbose mode
    static private boolean debugLog = false;

    /**
     * The main property of this class - connection to JDBC
     */
    private Connection connection = null;

    /**
     * Constructor of class
     */ {
        if (debugLog) System.out.println("H2Conn init anonymous block");
        try {
            // Register JDBC driver
            Class.forName(JDBC_DRIVER);
            // Open a connection
            connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (debugLog) System.out.println("H2Conn init anonymous block end");
    }

    /**
     * Close connection
     * <p>
     * Needed to close connection.
     * If not - Tomcat will not update WebServlet automatically.
     */
    public void close() {
        if (debugLog) System.out.println("H2Conn.close()");
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (debugLog) System.out.println("H2Conn.close() end");
    }

    /**
     * JDBC connection
     *
     * @return connection class
     */
    @Override
    public Connection connection() {
        try {
            if (debugLog) System.out.println("H2Conn.connection()");
            return connection;
        } finally {
            if (debugLog) System.out.println("H2Conn.connection() end");
        }
    }
}