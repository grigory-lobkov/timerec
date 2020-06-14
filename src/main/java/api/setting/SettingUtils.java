package api.setting;

import model.ServiceSettingRow;
import model.SettingRow;
import storage.ITable;
import storage.StorageFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class SettingUtils implements ServletContextListener {

    private ITable<SettingRow> storage = StorageFactory.getSettingInstance();
    private boolean debugLog = true;
    
    private final String CLIENT_LIMIT_DAILY_ALIAS = "CLIENT_LIMIT_DAILY";
    private final String CLIENT_LIMIT_WEEKLY_ALIAS = "CLIENT_LIMIT_WEEKLY";
    private final String CLIENT_LIMIT_MONTHLY_ALIAS = "CLIENT_LIMIT_MONTHLY";

    /**
     * Quick service settings access
     * Key = service_id
     */
    private Map<Long, ServiceSettingRow> serviceSettings;

    /**
     * Quick all settings access
     */
    private ServiceSettingRow allSetting;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        if (debugLog) System.out.println("SettingUtils.contextInitialized()");

        readStorage();
    }

    private void readStorage() {
        Map<Long, ServiceSettingRow> serviceSettings = new Hashtable<>();
        allSetting = new ServiceSettingRow();

        try {
            List<SettingRow> list = storage.select();
            for (SettingRow s : list) {
                Integer intValue = stringToInteger(s.value);
                if (intValue > 0) {
                    ServiceSettingRow sSetting;
                    if (s.service_id > 0) {
                        sSetting = serviceSettings.get(s.service_id);
                        if (sSetting == null) {
                            sSetting = new ServiceSettingRow();
                            serviceSettings.put(s.service_id, sSetting);
                        }
                    } else {
                        sSetting = allSetting;
                    }
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
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServiceSettingRow getServiceSetting(long service_id) {
        ServiceSettingRow sSetting = serviceSettings.get(service_id);
        if (sSetting == null) {
            sSetting = new ServiceSettingRow();
            serviceSettings.put(service_id, sSetting);
        }
        return sSetting;
    }

    public void updateServiceSetting(long service_id, ServiceSettingRow newSetting) {
        ServiceSettingRow mySetting = serviceSettings.get(service_id);
        if (mySetting == null) {
            mySetting = new ServiceSettingRow();
            serviceSettings.put(service_id, mySetting);
        }
        updateMySetting(service_id, mySetting, newSetting);
    }

    private void updateMySetting(Long service_id, ServiceSettingRow mySetting, ServiceSettingRow newSetting) {
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

    public ServiceSettingRow getAllSetting() {
        return allSetting;
    }

    public void updateAllSetting(ServiceSettingRow newSetting) {
        updateMySetting(null, allSetting, newSetting);
    }

    private Integer stringToInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        if (debugLog) System.out.println("SettingUtils.contextDestroyed()");
    }

}