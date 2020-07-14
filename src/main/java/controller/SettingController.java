package controller;

import model.ServiceSettingRow;
import model.SettingRow;
import storage.ITable;
import storage.StorageFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SettingController implements ServletContextListener {

    private static ITable<SettingRow> storage = StorageFactory.getSettingInstance();
    private static boolean debugLog = false;

    private static final String CLIENT_LIMIT_DAILY_ALIAS = "CLIENT_LIMIT_DAILY";
    private static final String CLIENT_LIMIT_WEEKLY_ALIAS = "CLIENT_LIMIT_WEEKLY";
    private static final String CLIENT_LIMIT_MONTHLY_ALIAS = "CLIENT_LIMIT_MONTHLY";
    private static final String ALERT_PATHS_ALIAS = "ALERT_PATHS";

    /**
     * Quick service settings access
     * Key = service_id
     */
    private static Map<Long, ServiceSettingRow> serviceSettings;

    /**
     * Quick all settings access
     */
    private static ServiceSettingRow allSetting;

    private static Map<String, SettingRow> otherSettings;

    @Override
    public synchronized void contextInitialized(ServletContextEvent servletContextEvent) {
        if (debugLog) System.out.println("SettingController.contextInitialized()");

        readStorage();
    }

    private static void readStorage() {
        serviceSettings = new Hashtable<>();
        otherSettings = new Hashtable<>();
        allSetting = new ServiceSettingRow();

        try {
            List<SettingRow> list = storage.select();
            for (SettingRow s : list) {
                ServiceSettingRow sSetting;
                if (s.service_id > 0) {
                    sSetting = serviceSettings.get(s.service_id);
                    if (sSetting == null) {
                        sSetting = new ServiceSettingRow();
                        serviceSettings.put(s.service_id, sSetting);
                    }
                } else {
                    sSetting = allSetting;
                    otherSettings.put(s.alias, s);
                }
                readMySetting(s, sSetting);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void readMySetting(SettingRow s, ServiceSettingRow sSetting) {
        Integer intValue = stringToInteger(s.value);
        if (intValue != null && intValue > 0) {
            switch (s.alias) {
                case CLIENT_LIMIT_DAILY_ALIAS:
                    sSetting.limitPerDay = intValue;
                    sSetting.limitPerDayRow = s;
                    break;
                case CLIENT_LIMIT_WEEKLY_ALIAS:
                    sSetting.limitPerWeek = intValue;
                    sSetting.limitPerWeekRow = s;
                    break;
                case CLIENT_LIMIT_MONTHLY_ALIAS:
                    sSetting.limitPerMonth = intValue;
                    sSetting.limitPerMonthRow = s;
                    break;
            }
        } else {
            switch (s.alias) {
                case ALERT_PATHS_ALIAS:
                    sSetting.alertPaths = s.value;
                    sSetting.alertPathsRow = s;
                    break;
            }
        }
    }

    private static Integer stringToInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static synchronized ServiceSettingRow getServiceSetting(long service_id) {
        ServiceSettingRow sSetting = serviceSettings.get(service_id);
        if (sSetting == null) {
            sSetting = new ServiceSettingRow();
            serviceSettings.put(service_id, sSetting);
        }
        return sSetting;
    }

    public static synchronized void setServiceSetting(long service_id, ServiceSettingRow newSetting) {
        ServiceSettingRow mySetting = serviceSettings.get(service_id);
        if (mySetting == null) {
            mySetting = new ServiceSettingRow();
            serviceSettings.put(service_id, mySetting);
        }
        updateMySetting(service_id, mySetting, newSetting);
    }

    public static synchronized SettingRow getSetting(String alias) {
        return otherSettings.get(alias);
    }

    public static synchronized List<SettingRow> getSettings() {
        return new ArrayList<>(otherSettings.values());
    }

    public static synchronized void setSetting(SettingRow setting) {
        SettingRow old = otherSettings.get(setting.alias);
        try {
            if (old != null) {
                setting.setting_id = old.setting_id;
                storage.update(setting);
                otherSettings.remove(setting.alias);
            } else {
                storage.insert(setting);
            }
            otherSettings.put(setting.alias, setting);
            readMySetting(setting, allSetting);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized ServiceSettingRow getAllSetting() {
        return allSetting;
    }

    public static synchronized void setAllSetting(ServiceSettingRow newSetting) {
        updateMySetting(null, allSetting, newSetting);
    }


    private static void updateMySetting(Long service_id, ServiceSettingRow mySetting, ServiceSettingRow newSetting) {
        try {
            // DAILY
            if (mySetting.limitPerDay != newSetting.limitPerDay) {
                if (mySetting.limitPerDayRow != null) {
                    // setting row exists
                    if (newSetting.limitPerDay == 0 && service_id > 0) {
                        storage.delete(mySetting.limitPerDayRow.setting_id);
                        mySetting.limitPerDayRow = null;
                    } else {
                        mySetting.limitPerDayRow.value = Integer.toString(newSetting.limitPerDay);
                        storage.update(mySetting.limitPerDayRow);
                    }
                } else {
                    // setting row does not exists
                    SettingRow s = new SettingRow();
                    s.service_id = service_id;
                    s.alias = CLIENT_LIMIT_DAILY_ALIAS;
                    s.value = Integer.toString(newSetting.limitPerDay);
                    storage.insert(s);
                    mySetting.limitPerDayRow = s;
                }
            }
            mySetting.limitPerDay = newSetting.limitPerDay;

            // WEEKLY
            if (mySetting.limitPerWeek != newSetting.limitPerWeek) {
                if (mySetting.limitPerWeekRow != null) {
                    // setting row exists
                    if (newSetting.limitPerWeek == 0 && service_id > 0) {
                        storage.delete(mySetting.limitPerWeekRow.setting_id);
                        mySetting.limitPerWeekRow = null;
                    } else {
                        mySetting.limitPerWeekRow.value = Integer.toString(newSetting.limitPerWeek);
                        storage.update(mySetting.limitPerWeekRow);
                    }
                } else {
                    // setting row does not exists
                    SettingRow s = new SettingRow();
                    s.service_id = service_id;
                    s.alias = CLIENT_LIMIT_WEEKLY_ALIAS;
                    s.value = Integer.toString(newSetting.limitPerWeek);
                    storage.insert(s);
                    mySetting.limitPerWeekRow = s;
                }
            }
            mySetting.limitPerWeek = newSetting.limitPerWeek;

            // MONTHLY
            if (mySetting.limitPerMonth != newSetting.limitPerMonth) {
                if (mySetting.limitPerMonthRow != null) {
                    // setting row exists
                    if (newSetting.limitPerMonth == 0 && service_id > 0) {
                        storage.delete(mySetting.limitPerMonthRow.setting_id);
                        mySetting.limitPerMonthRow = null;
                    } else {
                        mySetting.limitPerMonthRow.value = Integer.toString(newSetting.limitPerMonth);
                        storage.update(mySetting.limitPerMonthRow);
                    }
                } else {
                    // setting row does not exists
                    SettingRow s = new SettingRow();
                    s.service_id = service_id;
                    s.alias = CLIENT_LIMIT_MONTHLY_ALIAS;
                    s.value = Integer.toString(newSetting.limitPerMonth);
                    storage.insert(s);
                    mySetting.limitPerMonthRow = s;
                }
            }
            mySetting.limitPerMonth = newSetting.limitPerMonth;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (debugLog) System.out.println("SettingController.contextDestroyed()");
    }

}