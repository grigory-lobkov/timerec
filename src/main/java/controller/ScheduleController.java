package controller;

import model.RepeatRow;
import model.ScheduleRow;
import model.ServiceRow;
import model.UserRow;
import storage.IMultiRowTable;
import storage.IScheduleTable;
import storage.ITable;
import storage.StorageFactory;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ScheduleController {

    private static ITable<ServiceRow> storageService = StorageFactory.getServiceInstance();
    private static IMultiRowTable<RepeatRow> storageRepeat = StorageFactory.getRepeatInstance();
    private static IScheduleTable<ScheduleRow> storageSchedule = StorageFactory.getScheduleInstance();

    public static List<ScheduleRow> getScheduleByService(long service_id, ZonedDateTime dateStart, ZonedDateTime dateEnd, UserRow user) {
        //System.out.println("service_id="+service_id);
        //System.out.println("dateStart="+dateStart);
        //System.out.println("dateEnd="+dateEnd);
        //System.out.println("user="+user);
        List<ScheduleRow> result = new ArrayList<>();
        List<ScheduleRow> planList = new ArrayList<>();
        int deltaSec = UserController.getTzOffsetSeconds(user);

        try {
            //
            // GENERATE PLAN by REPEAT
            //

            ServiceRow service = storageService.select(service_id);
            ZonedDateTime dateNow = ZonedDateTime.now();
            int localOffset = dateNow.getOffset().getTotalSeconds();
            List<RepeatRow> repeats = storageRepeat.select(service_id);
            // from local to user timestamp
            for (RepeatRow r : repeats) {
                r.time_from += deltaSec - localOffset;
                r.time_to += deltaSec - localOffset;
            }
            long dow = dateStart.getDayOfWeek().getValue();
            ZonedDateTime weekStart = dateStart.minusDays(dow).truncatedTo(ChronoUnit.DAYS);
            Timestamp dbQueryStart = new Timestamp(weekStart.toInstant().toEpochMilli());
            // weeks loop
            while (weekStart.compareTo(dateEnd) <= 0) {
                //System.out.println("weekStart="+weekStart);
                // week days loop
                for (RepeatRow r : repeats) {
                    ZonedDateTime day = weekStart.plusDays(r.dow);
                    ZonedDateTime dayFrom = day.plusSeconds(r.time_from);
                    ZonedDateTime dayTo = day.plusSeconds(r.time_to);
                    ZonedDateTime dayNow = dayFrom;
                    ZonedDateTime dayNowTo = dayNow.plusMinutes(r.duration);
                    // in-day repeats
                    while (dayNowTo.compareTo(dayTo) <= 0) {
                        if (dayNowTo.compareTo(dateNow) > 0) {
                            ScheduleRow s = new ScheduleRow();
                            s.date_from = new Timestamp(dayNow.toInstant().toEpochMilli());
                            //System.out.println("s.date_from="+s.date_from+" ");
                            s.duration = r.duration;
                            s.title = "";
                            s.user_name = "";
                            s.service_name = service.name;
                            s.description = "";
                            s.service_id = service_id;
                            s.is_passed = 0;
                            planList.add(s);
                        }
                        dayNow = dayNowTo;
                        dayNowTo = dayNow.plusMinutes(r.duration);
                    }
                }
                weekStart = weekStart.plusDays(7);
            }
            Timestamp dbQueryEnd = new Timestamp(weekStart.toInstant().toEpochMilli());

            //
            // QUERY DATABASE
            //

            List<ScheduleRow> dbList = storageSchedule.selectByService(service_id, dbQueryStart, dbQueryEnd); // gives sorted by date_from
            // check if planned
            int allPlanned = planList.size();
            int idxPlanned = 0; // index of bigger element than read from database
            ScheduleRow planned = idxPlanned < allPlanned ? planList.get(idxPlanned) : null;
            for (ScheduleRow schedule : dbList) {
                //System.out.println(schedule.title);
                schedule.date_from = new Timestamp(schedule.date_from.getTime() + deltaSec * 1000); // to user timestamp
                if (planned != null) {
                    int cmp = planned.date_from.compareTo(schedule.date_from);
                    //System.out.println("cmp="+cmp+" planned="+planned.date_from+" schedule="+schedule.date_from);
                    if (cmp == 0) {
                        schedule.is_passed = 0;
                        idxPlanned++;
                        planned = idxPlanned < allPlanned ? planList.get(idxPlanned) : null;
                    } else if (cmp < 0) {
                        while (true) {
                            result.add(planned);
                            idxPlanned++;
                            planned = idxPlanned < allPlanned ? planList.get(idxPlanned) : null;
                            if (planned == null) break;
                            int cmp2 = planned.date_from.compareTo(schedule.date_from);
                            //System.out.println("cmp2="+cmp2+" planned="+planned.date_from+" schedule="+schedule.date_from);
                            if (cmp2 >= 0) {
                                if(cmp2==0) {
                                    idxPlanned++;
                                    planned = idxPlanned < allPlanned ? planList.get(idxPlanned) : null;
                                }
                                schedule.is_passed = 0;
                                break;
                            }
                        }
                    } else {
                        schedule.is_passed = 1;
                    }
                }
                result.add(schedule);
            }
            if (planned != null) {
                result.add(planned);
                //System.out.println("add planned="+planned.date_from);
                idxPlanned++;
                while (idxPlanned < allPlanned) {
                    result.add(planList.get(idxPlanned));
                    //System.out.println("add planned="+planList.get(idxPlanned).date_from);
                    idxPlanned++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    public static List<ScheduleRow> getScheduleByUser(ZonedDateTime dateStart, ZonedDateTime dateEnd, UserRow user) {
        List<ScheduleRow> scheduleList = new ArrayList<>();
        int deltaSec = UserController.getTzOffsetSeconds(user);

        try {
            Timestamp dbQueryStart = new Timestamp(dateStart.toInstant().toEpochMilli()- deltaSec * 1000);
            Timestamp dbQueryEnd = new Timestamp(dateEnd.toInstant().toEpochMilli()- deltaSec * 1000);
            scheduleList = storageSchedule.selectByUser(user.user_id, dbQueryStart, dbQueryEnd); // gives sorted by date_start
            
            Timestamp timeNow = new Timestamp(System.currentTimeMillis());
            for(ScheduleRow schedule: scheduleList) {
                //System.out.println("timeNow="+timeNow+" schedule.date_from="+schedule.date_from);
                if(timeNow.compareTo(schedule.date_from) < 0)
                    schedule.is_passed = 0;
                schedule.date_from = new Timestamp(schedule.date_from.getTime() + deltaSec * 1000); // to user timestamp
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return scheduleList;
    }

    public static boolean makeRecord(ScheduleRow row, UserRow client, UserRow user) throws Exception {
        boolean isUpdate = row.schedule_id > 0; // if schedule was cancelled
        int deltaSec = UserController.getTzOffsetSeconds(client);
        row.user_id = client.user_id;
        row.date_from = new Timestamp(row.date_from.getTime() - deltaSec * 1000);

        boolean result = isUpdate ? storageSchedule.update(row) : storageSchedule.insert(row);
        if(result) {
            AlertController.alertNewSchedule(client, row, user);
        }
        return result;
    }

    public static boolean cancelRecord(ScheduleRow row, UserRow client, UserRow user) throws Exception {
        AlertController.alertCancelSchedule(client, row, user); // we should alert before modifications (we can also copy UserRow for that)
        row.user_id = 0;
        //row.title = ""; // may be useful
        //row.description = "";
        boolean result = storageSchedule.update(row);
        return result;
    }

}
