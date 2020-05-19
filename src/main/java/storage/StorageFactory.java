package storage;

import storage.jdbc.IDBConn;
import storage.jdbc.ServiceStorage;
import storage.jdbc.h2.H2Conn;
import web.model.Service;

import java.sql.Connection;

/**
 * Storage Singleton factory
 */
public class StorageFactory {

    public static IDBConn dbConn = new H2Conn();
    public static Connection connection = dbConn.connection();

    static private volatile IStorage<Service> instance = null;

    /**
     * Generate Service storage actions
     *
     * @return singleton instance
     */
    static public IStorage<Service> getServiceInstance() {
        if (instance == null)
            synchronized (StorageFactory.class) {
                if (instance == null) {
                    instance = new ServiceStorage(connection);
                }
            }
        return instance;
    }

}