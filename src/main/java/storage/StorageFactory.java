package storage;

import integration.Integrator;
import model.*;
import storage.connectImpl.H2ConnectionPool;
import storage.connectImpl.MariadbConnectionPool;
import storage.connectImpl.PostgresConnectionPool;
import storage.tableImpl.*;

import java.util.NoSuchElementException;

/**
 * Storage Singleton factory
 */
public class StorageFactory {

    // Database type
    static private final String DB_TYPE = Integrator.getProperty("DB_TYPE");
    static private final String DB_DRIVER = Integrator.getProperty("DB_DRIVER");
    static private final String DB_URL = Integrator.getProperty("DB_URL");

    // JDBC maximum concurrent connections
    static private final Integer DB_MAX_POOL_SIZE = Integrator.getIntProperty("DB_MAX_POOL_SIZE");

    // JDBC timeout (seconds) to acquire new connection from the pool
    static private final Integer DB_CONNECTION_TIMEOUT = Integrator.getIntProperty("DB_CONNECTION_TIMEOUT");

    //  Database credentials
    static private final String DB_USER = Integrator.getProperty("DB_USER");
    static private final String DB_PASSWORD = Integrator.getProperty("DB_PASSWORD");

    static public IConnectionPool dbPool = getPool();

    static private volatile ITable<ServiceRow> serviceInstance = null;

    /**
     * Generate Service storage actions
     *
     * @return singleton instance
     */
    static public IConnectionPool getPool() {
        System.out.println("DB_TYPE=" + DB_TYPE + "\nDB_URL=" + DB_URL + "\nDB_USER=" + DB_USER);
        switch (DB_TYPE != null ? DB_TYPE.toUpperCase() : "") {
            case "POSTGRES":
                return new PostgresConnectionPool(DB_DRIVER, DB_URL, DB_MAX_POOL_SIZE, DB_CONNECTION_TIMEOUT, DB_USER, DB_PASSWORD);
            case "MARIADB":
                return new MariadbConnectionPool(DB_DRIVER, DB_URL, DB_MAX_POOL_SIZE, DB_CONNECTION_TIMEOUT, DB_USER, DB_PASSWORD);
            case "H2":
                return new H2ConnectionPool(DB_DRIVER, DB_URL, DB_MAX_POOL_SIZE, DB_CONNECTION_TIMEOUT, DB_USER, DB_PASSWORD);
            case "":
                throw new NoSuchElementException("Environment variable DB_TYPE is not set");
            default:
                throw new NoSuchElementException("Environment variable DB_TYPE=" + DB_TYPE + " have unsupported value");
        }
    }

    /**
     * Generate Service storage actions
     *
     * @return singleton instance
     */
    static public ITable<ServiceRow> getServiceInstance() {
        if (serviceInstance == null)
            synchronized (StorageFactory.class) {
                if (serviceInstance == null) {
                    serviceInstance = new ServiceTable(dbPool);
                }
            }
        return serviceInstance;
    }


    static private volatile ITable<UserRow> userInstance = null;

    /**
     * Generate User storage actions
     *
     * @return singleton instance
     */
    static public ITable<UserRow> getUserInstance() {
        if (userInstance == null)
            synchronized (StorageFactory.class) {
                if (userInstance == null) {
                    userInstance = new UserTable(dbPool);
                }
            }
        return userInstance;
    }


    static private volatile ITable<AccessRow> accessInstance = null;

    /**
     * Generate Access storage actions
     *
     * @return singleton instance
     */
    public static ITable<AccessRow> getAccessInstance() {
        if (accessInstance == null)
            synchronized (StorageFactory.class) {
                if (accessInstance == null) {
                    accessInstance = new AccessTable(dbPool);
                }
            }
        return accessInstance;
    }


    static private volatile IMultiRowTable<RepeatRow> repeatInstance = null;

    /**
     * Generate Access storage actions
     *
     * @return singleton instance
     */
    public static IMultiRowTable<RepeatRow> getRepeatInstance() {
        if (repeatInstance == null)
            synchronized (StorageFactory.class) {
                if (repeatInstance == null) {
                    repeatInstance = new RepeatTable(dbPool);
                }
            }
        return repeatInstance;
    }


    static private volatile ITable<SettingRow> settingInstance = null;

    /**
     * Generate Settings storage actions
     *
     * @return singleton instance
     */
    public static ITable<SettingRow> getSettingInstance() {
        if (settingInstance == null)
            synchronized (StorageFactory.class) {
                if (settingInstance == null) {
                    settingInstance = new SettingTable(dbPool);
                }
            }
        return settingInstance;
    }


    static private volatile IScheduleTable<ScheduleRow> scheduleInstance = null;

    /**
     * Generate Schedule storage actions
     *
     * @return singleton instance
     */
    public static IScheduleTable<ScheduleRow> getScheduleInstance() {
        if (scheduleInstance == null)
            synchronized (StorageFactory.class) {
                if (scheduleInstance == null) {
                    scheduleInstance = new ScheduleTable(dbPool);
                }
            }
        return scheduleInstance;
    }


    static private volatile ITable<RoleRow> roleInstance = null;

    /**
     * Generate Role storage actions
     *
     * @return singleton instance
     */
    public static ITable<RoleRow> getRoleInstance() {
        if (roleInstance == null)
            synchronized (StorageFactory.class) {
                if (roleInstance == null) {
                    roleInstance = new RoleTable(dbPool);
                }
            }
        return roleInstance;
    }


    static private volatile ITable<ImageRow> imageInstance = null;

    /**
     * Generate Role storage actions
     *
     * @return singleton instance
     */
    public static ITable<ImageRow> getImageInstance() {
        if (imageInstance == null)
            synchronized (StorageFactory.class) {
                if (imageInstance == null) {
                    imageInstance = new ImageTable(dbPool);
                }
            }
        return imageInstance;
    }


    static private volatile ITable<TzRow> tzInstance = null;

    /**
     * Generate Tz storage actions
     *
     * @return singleton instance
     */
    public static ITable<TzRow> getTzInstance() {
        if (tzInstance == null)
            synchronized (StorageFactory.class) {
                if (tzInstance == null) {
                    tzInstance = new TzTable(dbPool);
                }
            }
        return tzInstance;
    }

    /**
     * Generate parameter-based storage {@code ITable} object
     * Used to determine owner inside AccessFilterApi. Deprecated.
     *
     * @param object name of object
     * @return singleton instance
     */
//    public static ITable getInstance(String object) {
//        switch(object) {
//            case "service": return getServiceInstance();
//            case "user": return getUserInstance();
//            case "access": return getAccessInstance();
//        }
//        return null;
//    }

}
