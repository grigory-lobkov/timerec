package storage.connectImpl;

import org.h2.jdbcx.JdbcConnectionPool;
import storage.IConnectionPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton class, returning one JDBC connection
 */
public class H2ConnectionPool implements IConnectionPool {

    /**
     * The main property of this class - connection to JDBC
     */
    private final DataSource pool;

    /**
     * Constructor of class
     */
    public H2ConnectionPool(DataSource pool) {
        this.pool = pool;
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
