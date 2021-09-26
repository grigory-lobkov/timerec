package storage.connectImpl;

import storage.IConnectionPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton class, returning one JDBC connection
 */
public class MariadbConnectionPool implements IConnectionPool {

    // JDBC driver name and database URL
    private String dbDriver = "org.mariadb.jdbc.Driver";

    /**
     * The main property of this class - connection to JDBC
     */
    private final DataSource pool;

    /**
     * Constructor of class
     */
    public MariadbConnectionPool(DataSource pool) throws ClassNotFoundException {
        Class.forName(dbDriver == null ? this.dbDriver : dbDriver);
        this.pool = pool;
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
     * Get one JDBC connection
     *
     * @return connection class
     */
    @Override
    public Connection connection() {
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
