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


    // other table fields
    public String client_name;

}
