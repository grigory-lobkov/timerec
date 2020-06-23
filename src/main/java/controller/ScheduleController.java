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

            //ServiceRow service = storageService.select(service_id);
            ZonedDateTime dateNow = ZonedDateTime.now();
            int localOffset = dateNow.getOffset().getTotalSeconds();
            List<RepeatRow> repeats = storageRepeat.select(service_id);
            // from local to user timestamp
            for (RepeatRow r : repeats) {
                r.time_from += deltaSec - localOffset;
                r.time_to += deltaSec - localOffset;
            }
            //System.out.println("dateNow="+dateNow);
            long dow = dateStart.getDayOfWeek().getValue();
            ZonedDateTime weekStart = dateStart.minusDays(dow).truncatedTo(ChronoUnit.DAYS);
            Timestamp dbQueryStart = new java.sql.Timestamp(weekStart.toInstant().toEpochMilli());
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
                            s.date_from = new java.sql.Timestamp(dayNow.toInstant().toEpochMilli());
                            //System.out.println("s.date_from="+s.date_from+" ");
                            s.duration = r.duration;
                            s.title = "";
                            s.user_name = "";
                            s.description = "";//service.name;
                            s.service_id = service_id;
                            planList.add(s);
                        }
                        dayNow = dayNowTo;
                        dayNowTo = dayNow.plusMinutes(r.duration);
                    }
                }
                weekStart = weekStart.plusDays(7);
            }
            Timestamp dbQueryEnd = new java.sql.Timestamp(weekStart.toInstant().toEpochMilli());

            //
            // QUERY DATABASE
            //

            List<ScheduleRow> dbList = storageSchedule.selectByService(service_id, dbQueryStart, dbQueryEnd); // gives sorted by date_start
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
                                break;
                            }
                        }
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

    public static boolean makeRecord(ScheduleRow row, UserRow user) throws Exception {
        int deltaSec = UserController.getTzOffsetSeconds(user);
        row.user_id = user.user_id;
        row.date_from = new Timestamp(row.date_from.getTime() - deltaSec * 1000);

        boolean result = storageSchedule.insert(row);
        if(result) {
            AlertController.alertNewSchedule(user, row);
        }
        return result;
    }
}
