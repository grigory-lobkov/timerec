package storage.connectImpl;

import storage.IConnectionPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton class, returning one JDBC connection
 */
public class PostgresConnectionPool implements IConnectionPool {

    public String getDbDriver() {
        return "org.postgresql.Driver";
    }

    public String fromDual() {
        return "";
    }

    public String preSeqNextval() {
        return "nextval('";
    }

    public String postSeqNextval() {
        return "')";
    }

    public String preSeqCurrval() {
        return "currval('";
    }

    public String postSeqCurrval() {
        return "')";
    }

    public String preText() {
        return "";
    }

    public String postText() {
        return "::text";
    }

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Connection connection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
