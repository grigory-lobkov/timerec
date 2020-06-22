package controller;

import model.RepeatRow;
import model.ScheduleRow;
import model.UserRow;
import storage.IMultiRowTable;
import storage.IScheduleTable;
import storage.StorageFactory;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ScheduleController {

    private static IMultiRowTable<RepeatRow> storageRepeat = StorageFactory.getRepeatInstance();
    private static IScheduleTable<ScheduleRow> storageSchedule = StorageFactory.getScheduleInstance();

    public static List<ScheduleRow> getSchedule(long service_id, ZonedDateTime dateStart, ZonedDateTime dateEnd, UserRow user) {
        System.out.println("service_id="+service_id);
        System.out.println("dateStart="+dateStart);
        System.out.println("dateEnd="+dateEnd);
        System.out.println("user="+user);
        List<ScheduleRow> list = new ArrayList<>();
        int deltaSec = UserController.getTzOffsetSeconds(user);

        try {
            //ServiceRow service = storageService.select(service_id);
            ZonedDateTime dateNow = ZonedDateTime.now();
            int localOffset = dateNow.getOffset().getTotalSeconds();
            List<RepeatRow> repeats = storageRepeat.select(service_id);
            for (RepeatRow r : repeats) {
                r.time_from += deltaSec-localOffset;
                r.time_to += deltaSec-localOffset;
            }
            System.out.println("dateNow="+dateNow);
            long dow = dateStart.getDayOfWeek().getValue();
            ZonedDateTime weekStart = dateStart.minusDays(dow).truncatedTo(ChronoUnit.DAYS);
            // weeks loop
            while(weekStart.compareTo(dateEnd) <= 0) {
                System.out.println("weekStart="+weekStart);
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
                            System.out.println("s.date_from="+s.date_from+" ");
                            s.duration = r.duration;
                            s.title = "";
                            s.user_name = "";
                            s.description = "";//service.name;
                            s.service_id = service_id;
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

    public static boolean makeRecord(ScheduleRow row, UserRow user) throws Exception {
        int deltaSec = UserController.getTzOffsetSeconds(user);
        row.user_id = user.user_id;
        row.date_from = new Timestamp(row.date_from.getTime() - deltaSec * 1000);
        System.out.println("row.date_from="+row.date_from);
        boolean result = storageSchedule.insert(row);
        AlertController.alertNewSchedule(user, row);
        return result;
    }
}
