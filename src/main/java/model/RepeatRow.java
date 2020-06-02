package model;

import java.sql.Date;

public class RepeatRow {

    // identifier
    public long repeat_id;

    // link to service
    public long service_id;

    // day of week
    public int dow;

    // length in minutes
    public int duration;

    // period of time in day
    public Date timeFrom;
    public Date timeTo;

}
