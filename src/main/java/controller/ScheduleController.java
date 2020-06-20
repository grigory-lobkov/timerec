package controller;

import model.RepeatRow;
import model.ScheduleRow;
import model.ServiceRow;
import model.UserRow;
import storage.IMultiRowTable;
import storage.ITable;
import storage.StorageFactory;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ScheduleController {

    private static IMultiRowTable<RepeatRow> storageRepeat = StorageFactory.getRepeatInstance();
    private static ITable<ServiceRow> storageService = StorageFactory.getServiceInstance();

    public static List<ScheduleRow> getSchedule(long service_id, ZonedDateTime dateStart, ZonedDateTime dateEnd, UserRow user) {
        List<ScheduleRow> list = new ArrayList<>();
        int deltaSec = UserController.getTzOffsetSeconds(user);

//        ScheduleRow row = new ScheduleRow();
//        row.date_from = new java.sql.Timestamp((new java.util.Date()).getTime());
//        row.duration = 15;
//        row.title = "ExTitle"+service_id;
//        row.client_name = "ExClientName"+service_id;
//        row.description = "Example of description of service_id="+service_id;
//        list.add(row);
//
//        row = new ScheduleRow();
//        row.date_from = new java.sql.Timestamp((new java.util.Date()).getTime()+ TimeUnit.MINUTES.toMillis(15));
//        row.duration = 15;
//        row.title = "Ex2Title"+service_id;
//        row.client_name = "Ex2ClientName"+service_id;
//        row.description = "Ex2ample of description of service_id="+service_id;
//        list.add(row);

        try {
            ServiceRow service = storageService.select(service_id);
            List<RepeatRow> repeats = storageRepeat.select(service_id);
            for (RepeatRow r : repeats) {
                r.time_from += deltaSec;
                r.time_to += deltaSec;
            }
            ZonedDateTime dateNow = ZonedDateTime.now();
//            long curToStart = ChronoUnit.DAYS.between(dateStart, dateNow); // dateStart to now difference
//            long curToEnd = ChronoUnit.DAYS.between(dateEnd, dateNow); // dateEnd to now difference
            long dow = dateStart.getDayOfWeek().getValue();
            ZonedDateTime weekStart = dateStart.minusDays(dow).truncatedTo(ChronoUnit.DAYS);
            // weeks loop
            while(weekStart.compareTo(dateEnd) <= 0) {
                // week days loop
                for (RepeatRow r : repeats) {
                    ZonedDateTime day = weekStart.plusDays(r.dow);
                    ZonedDateTime dayFrom = day.plusSeconds(r.time_from);
                    ZonedDateTime dayTo = day.plusSeconds(r.time_to);
                    ZonedDateTime dayNow = dayFrom;
                    ZonedDateTime dayNowTo = dayNow.plusMinutes(r.duration);
                    // in-day repeats
                    while (dayNowTo.compareTo(dayTo) <= 0) {
                        if(dayNowTo.compareTo(dateNow) > 0) {
                            ScheduleRow s = new ScheduleRow();
                            s.date_from = new java.sql.Timestamp(dayNow.toInstant().toEpochMilli());
                            s.duration = r.duration;
                            s.title = "";
                            s.client_name = "";
                            s.description = service.name;
                            list.add(s);
                        }
                        dayNow = dayNowTo;
                        dayNowTo = dayNow.plusMinutes(r.duration);
                    }
                }
                weekStart = weekStart.plusDays(7);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return list;
    }

}
