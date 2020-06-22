package storage;

import model.*;
import storage.connectImpl.H2ConnectionPool;
import storage.tableImpl.*;

/**
 * Storage Singleton factory
 */
public class StorageFactory {

    public static IConnectionPool dbPool = new H2ConnectionPool();


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