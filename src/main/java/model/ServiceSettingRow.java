package model;

public class ServiceSettingRow {

    // daily limit
    public int limitPerDay = 0;

    // setting table row for updates
    public transient SettingRow limitPerDayRow = null;


    // weekly limit
    public int limitPerWeek = 0;

    // setting table row for updates
    public transient SettingRow limitPerWeekRow = null;


    // monthly limit
    public int limitPerMonth = 0;

    // setting table row for updates
    public transient SettingRow limitPerMonthRow = null;


    // service alerts paths
    public String alertPaths = "";

    // setting table row for alerts
    public transient SettingRow alertPathsRow = null;


    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
