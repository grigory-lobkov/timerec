package model;

public class SettingRow {

    // identifier
    public long setting_id;

    // short name
    public String alias;

    // title
    public String name;

    // descriptive specification
    public String description;

    // value
    public String value;

    // link to service
    public long service_id;

    // owner user_id
    public long owner_id;

    @Override
    public String toString() {
        return "SettingRow{" +
                "setting_id=" + setting_id +
                ", alias='" + alias + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", value='" + value + '\'' +
                ", service_id=" + service_id +
                ", owner_id=" + owner_id +
                '}';
    }
}
