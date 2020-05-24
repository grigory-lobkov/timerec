package storage;

import storage.jdbc.IDBConn;
import storage.jdbc.ServiceStorage;
import storage.jdbc.UserStorage;
import storage.jdbc.h2.H2Conn;
import web.model.Service;
import web.model.User;

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

}