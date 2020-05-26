package storage;

import storage.jdbc.AccessStorage;
import storage.jdbc.IDBConn;
import storage.jdbc.ServiceStorage;
import storage.jdbc.UserStorage;
import storage.jdbc.h2.H2Conn;
import model.Access;
import model.Service;
import model.User;

import java.sql.Connection;

/**
 * Storage Singleton factory
 */
public class StorageFactory {

    public static IDBConn dbConn = new H2Conn();
    public static Connection connection = dbConn.connection();


    static private volatile IStorage<Service> serviceInstance = null;

    /**
     * Generate Service storage actions
     *
     * @return singleton instance
     */
    static public IStorage<Service> getServiceInstance() {
        if (serviceInstance == null)
            synchronized (StorageFactory.class) {
                if (serviceInstance == null) {
                    serviceInstance = new ServiceStorage(connection);
                }
            }
        return serviceInstance;
    }


    static private volatile IStorage<User> userInstance = null;

    /**
     * Generate User storage actions
     *
     * @return singleton instance
     */
    static public IStorage<User> getUserInstance() {
        if (userInstance == null)
            synchronized (StorageFactory.class) {
                if (userInstance == null) {
                    userInstance = new UserStorage(connection);
                }
            }
        return userInstance;
    }


    static private volatile IStorage<Access> accessInstance = null;

    /**
     * Generate Access storage actions
     *
     * @return singleton instance
     */
    public static IStorage<Access> getAccessInstance() {
        if (accessInstance == null)
            synchronized (StorageFactory.class) {
                if (accessInstance == null) {
                    accessInstance = new AccessStorage(connection);
                }
            }
        return accessInstance;
    }


    /**
     * Generate parameter-based storage {@code IStorage} object
     *
     * @param object name of object
     * @return singleton instance
     */
    public static IStorage getInstance(String object) {
        switch(object) {
            case "service": return getServiceInstance();
            case "user": return getUserInstance();
            case "access": return getAccessInstance();
        }
        return null;
    }
}