package api.service;

import model.ScheduleRow;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class ScheduleUtils {

    //Type listType = new TypeToken<List<Transport>>() {}.getType();

    static class Transport {
        // https://fullcalendar.io/docs/event-object
        long id;
        String title;
        String start;
        String end;
        String backgroundColor;
        ScheduleRow extendedProps;
    }

    static String unzone(String zonedTime) {
        //2020-01-27T00:00:00+05:00
        String unzonedTime = zonedTime.substring(0,19)+"Z";
        return unzonedTime;
    }

    static Transport convertToTransport(ScheduleRow row) {
        Timestamp date_to = new Timestamp(row.date_from.getTime() + TimeUnit.MINUTES.toMillis(row.duration));

        Transport t = new Transport();
        t.id = row.schedule_id;
        if (row.user_id > 0) {
            t.title = row.service_name + " - " + row.title;
            if(row.is_passed == 0) {
                t.backgroundColor = "#7c7"; // future record
            } else {
                t.backgroundColor = "#ccc"; // past record
            }
        } else {
            t.title = "";
            t.backgroundColor = ""; // цвет по умолчанию
        }
        t.start = row.date_from.toString();
        t.end = date_to.toString();
        t.extendedProps = row;
        return t;
    }

}
