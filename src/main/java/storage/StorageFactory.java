package storage;

import storage.tableImpl.*;
import storage.connectImpl.H2Connect;
import model.Access;
import model.Service;
import model.User;

import java.sql.Connection;

/**
 * Storage Singleton factory
 */
public class StorageFactory {

    public static IConnect dbConn = new H2Connect();
    public static Connection connection = dbConn.connection();


    static private volatile ITable<Service> serviceInstance = null;

    /**
     * Generate Service storage actions
     *
     * @return singleton instance
     */
    static public ITable<Service> getServiceInstance() {
        if (serviceInstance == null)
            synchronized (StorageFactory.class) {
                if (serviceInstance == null) {
                    serviceInstance = new ServiceTable(connection);
                }
            }
        return serviceInstance;
    }


    static private volatile ITable<User> userInstance = null;

    /**
     * Generate User storage actions
     *
     * @return singleton instance
     */
    static public ITable<User> getUserInstance() {
        if (userInstance == null)
            synchronized (StorageFactory.class) {
                if (userInstance == null) {
                    userInstance = new UserTable(connection);
                }
            }
        return userInstance;
    }


    static private volatile ITable<Access> accessInstance = null;

    /**
     * Generate Access storage actions
     *
     * @return singleton instance
     */
    public static ITable<Access> getAccessInstance() {
        if (accessInstance == null)
            synchronized (StorageFactory.class) {
                if (accessInstance == null) {
                    accessInstance = new AccessTable(connection);
                }
            }
        return accessInstance;
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