package storage;

import model.AccessRow;
import model.RepeatRow;
import model.ServiceRow;
import storage.tableImpl.*;
import storage.connectImpl.H2Connect;
import model.UserRow;

import java.sql.Connection;

/**
 * Storage Singleton factory
 */
public class StorageFactory {

    public static IConnect dbConn = new H2Connect();


    static private volatile ITable<ServiceRow> serviceInstance = null;

    /**
     * Generate Service storage actions
     *
     * @return singleton instance
     */
    static public ITable<ServiceRow> getServiceInstance() {
        if (serviceInstance == null)
            synchronized (StorageFactory.class) {
                if (serviceInstance == null) {
                    serviceInstance = new ServiceTable(dbConn);
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
                    userInstance = new UserTable(dbConn);
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
                    accessInstance = new AccessTable(dbConn);
                }
            }
        return accessInstance;
    }


    static private volatile ITable<RepeatRow> repeatInstance = null;

    /**
     * Generate Access storage actions
     *
     * @return singleton instance
     */
    public static ITable<RepeatRow> getRepeatInstance() {
        if (repeatInstance == null)
            synchronized (StorageFactory.class) {
                if (repeatInstance == null) {
                    repeatInstance = new RepeatTable(dbConn);
                }
            }
        return repeatInstance;
    }

    /**
     * Generate parameter-based storage {@code ITable} object
     *
     * @param object name of object
     * @return singleton instance
     */
    public static ITable getInstance(String object) {
        switch(object) {
            case "service": return getServiceInstance();
            case "user": return getUserInstance();
            case "access": return getAccessInstance();
        }
        return null;
    }

}