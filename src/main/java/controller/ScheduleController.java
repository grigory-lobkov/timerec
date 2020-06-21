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
    //private static ITable<ServiceRow> storageService = StorageFactory.getServiceInstance();

    public static List<ScheduleRow> getSchedule(long service_id, ZonedDateTime dateStart, ZonedDateTime dateEnd, UserRow user) {
        List<ScheduleRow> list = new ArrayList<>();
        int deltaSec = UserController.getTzOffsetSeconds(user);

        try {
            //ServiceRow service = storageService.select(service_id);
            List<RepeatRow> repeats = storageRepeat.select(service_id);
            for (RepeatRow r : repeats) {
                r.time_from += deltaSec;
                r.time_to += deltaSec;
            }
            ZonedDateTime dateNow = ZonedDateTime.now();
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
                            s.description = "";//service.name;
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
