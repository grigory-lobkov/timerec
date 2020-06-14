package model;

public class ServiceSettingRow {

    // daily limit
    public int limitPerDay = 0;

    // setting table row for updates
    public SettingRow limitPerDayRow = null;


    // weekly limit
    public int limitPerWeek = 0;

    // setting table row for updates
    public SettingRow limitPerWeekRow = null;


    // monthly limit
    public int limitPerMonth = 0;

    // setting table row for updates
    public SettingRow limitPerMonthRow = null;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
