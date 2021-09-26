package storage;

import model.*;
import storage.tableImpl.*;

/**
 * Storage Singleton factory
 */
public class StorageFactory {

    public static IConnectionPool dbPool = HikariPooler.getPooler();

    private static volatile ITable<ServiceRow> serviceInstance = null;

    /**
     * Generate Service storage actions
     *
     * @return singleton instance
     */
    public static ITable<ServiceRow> getServiceInstance() {
        if (serviceInstance == null)
            synchronized (StorageFactory.class) {
                if (serviceInstance == null) {
                    serviceInstance = new ServiceTable(dbPool);
                }
            }
        return serviceInstance;
    }


    private static volatile ITable<UserRow> userInstance = null;

    /**
     * Generate User storage actions
     *
     * @return singleton instance
     */
    public static ITable<UserRow> getUserInstance() {
        if (userInstance == null)
            synchronized (StorageFactory.class) {
                if (userInstance == null) {
                    userInstance = new UserTable(dbPool);
                }
            }
        return userInstance;
    }


    private static volatile ITable<AccessRow> accessInstance = null;

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


    private static volatile IMultiRowTable<RepeatRow> repeatInstance = null;

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


    private static volatile ITable<SettingRow> settingInstance = null;

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


    private static volatile IScheduleTable<ScheduleRow> scheduleInstance = null;

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


    private static volatile ITable<RoleRow> roleInstance = null;

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


    private static volatile ITable<ImageRow> imageInstance = null;

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


    private static volatile ITable<TzRow> tzInstance = null;

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
