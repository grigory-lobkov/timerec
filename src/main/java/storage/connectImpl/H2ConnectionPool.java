package storage.connectImpl;

import com.zaxxer.hikari.HikariDataSource;
import storage.IConnectionPool;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Singleton class, returning one JDBC connection
 */
public class H2ConnectionPool implements IConnectionPool {

    public String getDbDriver() {
        return "org.h2.Driver";
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

    public String preSeqCurrval() {
        return "";
    }

    public String postSeqCurrval() {
        return ".currval";//?
    }

    public String preText() {
        return "";
    }

    public String postText() {
        return "";
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

    public void close() {
        if(dataSource.getClass() == HikariDataSource.class) {
            ((HikariDataSource) dataSource).close();
        }
    }
}
