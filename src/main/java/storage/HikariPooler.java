package storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import integration.Integrator;
import storage.connectImpl.H2ConnectionPool;
import storage.connectImpl.MariadbConnectionPool;
import storage.connectImpl.PostgresConnectionPool;

import java.util.NoSuchElementException;

public class HikariPooler {

    // Database type
    private static final String DB_TYPE = Integrator.getProperty("DB_TYPE");
    private static final String DB_URL = Integrator.getProperty("DB_URL");

    // JDBC maximum concurrent connections
    private static final Integer DB_MAX_POOL_SIZE = Integrator.getIntProperty("DB_MAX_POOL_SIZE");

    //  Database credentials
    private static final String DB_USER = Integrator.getProperty("DB_USER");
    private static final String DB_PASSWORD = Integrator.getProperty("DB_PASSWORD");

    public static IConnectionPool getPooler() {
        System.out.println("DB_TYPE=" + DB_TYPE + "\nDB_URL=" + DB_URL + "\nDB_USER=" + DB_USER);
        IConnectionPool pool;
        switch (DB_TYPE != null ? DB_TYPE.toUpperCase() : "") {
            case "POSTGRES":
                pool = new PostgresConnectionPool();
                break;
            case "MARIADB":
                pool = new MariadbConnectionPool();
                break;
            case "H2":
                pool = new H2ConnectionPool();
                break;
            case "":
                throw new NoSuchElementException("Environment variable DB_TYPE is not set");
            default:
                throw new NoSuchElementException("Environment variable DB_TYPE=" + DB_TYPE + " have unsupported value");
        }
        pool.setDataSource(getDataSource(DB_URL, DB_USER, DB_PASSWORD, DB_MAX_POOL_SIZE, pool.getDbDriver()));
        return pool;
    }

    private static HikariDataSource getDataSource(String dbUrl, String dbUser, String dbPassword, Integer dbMaxPoolSize, String dbDriver) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(dbDriver);
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPassword);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        if (dbMaxPoolSize != null) {
            config.setMinimumIdle(dbMaxPoolSize > 2 ? dbMaxPoolSize / 2 : 1);
            config.setMaximumPoolSize(dbMaxPoolSize);
        }
        return new HikariDataSource(config);
    }

}
