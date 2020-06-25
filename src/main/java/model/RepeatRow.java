package model;

public class RepeatRow {

    // identifier
    public long repeat_id;

    // link to service
    public long service_id;

    // day of week
    public int dow;

    // length in minutes
    public int duration;

    // period of time in seconds
    public int time_from;
    public int time_to;

    @Override
    public String toString() {
        return "RepeatRow{" +
                "dow=" + dow +
                ", " + time_from +
                " - " + time_to +
                ", duration=" + duration +
                '}';
    }
}
