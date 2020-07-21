package storage;

import model.*;
import storage.connectImpl.*;
import storage.tableImpl.*;

/**
 * Storage Singleton factory
 */
public class StorageFactory {

    public static IConnectionPool dbPool = new PgConnectionPool();

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