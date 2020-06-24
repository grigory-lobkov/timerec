package model;

import java.sql.Timestamp;

public class ScheduleRow {

    // identifier
    public long schedule_id;

    public long service_id;
    public long user_id;

    public Timestamp date_from;

    // length in minutes
    public int duration;

    // title
    public String title;
    // description
    public String description;


    // other fields
    public String user_name;
    public String service_name;
    public int is_passed = 1; // 1 - if passed, 0 - if in future (ScheduleController class sets it)

}
