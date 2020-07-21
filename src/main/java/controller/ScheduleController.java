package controller;

import model.*;
import storage.IMultiRowTable;
import storage.IScheduleTable;
import storage.ITable;
import storage.StorageFactory;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
            ZonedDateTime dateNow = ZonedDateTime.now().plusSeconds(deltaSec);

            //ZonedDateTime dateStart1 = dateStart.plusSeconds(deltaSec-localOffset);//.plusSeconds(deltaSec);
            //ZonedDateTime dateEnd1 = dateEnd.plusSeconds(deltaSec-localOffset);//.plusSeconds(deltaSec);

            List<RepeatRow> repeats = storageRepeat.select(service_id);
            // from local to user timestamp
            for (RepeatRow r : repeats) {
                r.time_from += deltaSec;
                r.time_to += deltaSec;
            }
            long dow = dateStart.getDayOfWeek().getValue();
            ZonedDateTime weekStart = dateStart.minusDays(dow).truncatedTo(ChronoUnit.DAYS);
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
                        if (dayNow.compareTo(dateNow) > 0 && dayNowTo.compareTo(dateEnd) < 0) {
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

            //
            // QUERY DATABASE
            //

            Timestamp dbQueryStart = new Timestamp(dateStart.minusSeconds(deltaSec).toInstant().toEpochMilli());
            Timestamp dbQueryEnd = new Timestamp(dateEnd.toInstant().toEpochMilli());
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
                                if (cmp2 == 0) {
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
            Timestamp dbQueryStart = new Timestamp(dateStart.toInstant().toEpochMilli() - deltaSec * 1000);
            Timestamp dbQueryEnd = new Timestamp(dateEnd.toInstant().toEpochMilli() - deltaSec * 1000);
            scheduleList = storageSchedule.selectByUser(user.user_id, dbQueryStart, dbQueryEnd); // gives sorted by date_start

            Timestamp timeNow = new Timestamp(System.currentTimeMillis() - deltaSec * 1000);
            for (ScheduleRow schedule : scheduleList) {
                System.out.println("timeNow="+timeNow+" schedule.date_from="+schedule.date_from);
                if (timeNow.compareTo(schedule.date_from) < 0)
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
        if (result) {
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


    public static void checkScheduleLimits(List<ScheduleRow> list, long service_id, ZonedDateTime dateStart, ZonedDateTime dateEnd, UserRow user) {

        ServiceSettingRow serviceSetting = SettingController.getServiceSetting(service_id);
        int allLimitDaily = getLimitSetting("ALL_SERVICES_CLIENT_LIMIT_DAILY");
        int allLimitWeekly = getLimitSetting("ALL_SERVICES_CLIENT_LIMIT_WEEKLY");
        int allLimitMonthly = getLimitSetting("ALL_SERVICES_CLIENT_LIMIT_MONTHLY");
        if (serviceSetting.limitPerWeek == 0 && serviceSetting.limitPerDay == 0 && serviceSetting.limitPerMonth == 0
                && allLimitWeekly == 0) {
            return;
        }

        int deltaSec = UserController.getTzOffsetSeconds(user);
        //ZonedDateTime dateNow = ZonedDateTime.now();
        final long msInHalfDay = 1000 * 60 * 60 * 12;
        final long msInDay = msInHalfDay * 2 - msInHalfDay;
        final long msInWeek = msInHalfDay * 2 * 7 - msInHalfDay;
        final long msInMonth = msInHalfDay * 2 * 30 - msInHalfDay;

        Timestamp dbQueryStart = new Timestamp(dateStart.truncatedTo(ChronoUnit.DAYS).minusDays(30).toInstant().toEpochMilli());
        Timestamp dbQueryEnd = new Timestamp(dateEnd.toInstant().toEpochMilli());

        try {
            List<ScheduleRow> dbList = storageSchedule.selectByUser(user.user_id, dbQueryStart, dbQueryEnd); // gives sorted by date_from
            ListIterator<ScheduleRow> iUser = dbList.listIterator();
            ListIterator<ScheduleRow> iSched = list.listIterator();
            for (ScheduleRow r: dbList) {
                r.date_from = new Timestamp(r.date_from.getTime() + deltaSec * 1000);
            }

            ScheduleRow nextUser = iUser.hasNext() ? iUser.next() : null;
            Deque<ScheduleRow> weekSQueue = new ArrayDeque<ScheduleRow>(dbList.size());
            Deque<ScheduleRow> weekAQueue = new ArrayDeque<ScheduleRow>(dbList.size());
            Deque<ScheduleRow> daySQueue = new ArrayDeque<ScheduleRow>(dbList.size());
            Deque<ScheduleRow> dayAQueue = new ArrayDeque<ScheduleRow>(dbList.size());
            Deque<ScheduleRow> monthSQueue = new ArrayDeque<ScheduleRow>(dbList.size());
            Deque<ScheduleRow> monthAQueue = new ArrayDeque<ScheduleRow>(dbList.size());

            int dayBanLastIdx = -1;
            int weekBanLastIdx = -1;
            int monthBanLastIdx = -1;

            while (iSched.hasNext()) {
                ScheduleRow rSched = iSched.next();
                if (rSched.user_id == 0) {

                    // fill queues
                    if (nextUser != null) {
                        if (nextUser.date_from.compareTo(rSched.date_from) <= 0) {
                            if (nextUser.service_id == service_id) {
                                daySQueue.add(nextUser);
                                weekSQueue.add(nextUser);
                                monthSQueue.add(nextUser);
                            }
                            dayAQueue.add(nextUser);
                            weekAQueue.add(nextUser);
                            monthAQueue.add(nextUser);
                            while (true) {
                                if (iUser.hasNext()) {
                                    nextUser = iUser.next();
                                } else {
                                    nextUser = null;
                                    break;
                                }
                                if (nextUser.date_from.compareTo(rSched.date_from) <= 0) {
                                    if (nextUser.service_id == service_id) {
                                        daySQueue.add(nextUser);
                                        weekSQueue.add(nextUser);
                                        monthSQueue.add(nextUser);
                                    }
                                    dayAQueue.add(nextUser);
                                    weekAQueue.add(nextUser);
                                    monthAQueue.add(nextUser);
                                } else {
                                    break;
                                }
                            }
                        }
                    }

                    // purge queues
                    long tm = rSched.date_from.getTime();
                    Timestamp purgeDTo = new Timestamp(tm - msInDay);
                    Timestamp purgeWTo = new Timestamp(tm - msInWeek);
                    Timestamp purgeMTo = new Timestamp(tm - msInMonth);
                    purgeQueueIfOlder(daySQueue, purgeDTo);
                    purgeQueueIfOlder(dayAQueue, purgeDTo);
                    purgeQueueIfOlder(weekSQueue, purgeWTo);
                    purgeQueueIfOlder(weekAQueue, purgeWTo);
                    purgeQueueIfOlder(monthSQueue, purgeMTo);
                    purgeQueueIfOlder(monthAQueue, purgeMTo);

                    // check to disable time in schedule
                    if (rSched.service_id == service_id) {
                        if (serviceSetting.limitPerDay > 0 && daySQueue.size() >= serviceSetting.limitPerDay)
                            dayBanLastIdx = banSchedule(list, iSched.nextIndex() - 1, dayBanLastIdx, purgeDTo);
                        if (serviceSetting.limitPerWeek > 0 && weekSQueue.size() >= serviceSetting.limitPerWeek)
                            weekBanLastIdx = banSchedule(list, iSched.nextIndex() - 1, weekBanLastIdx, purgeWTo);
                        if (serviceSetting.limitPerMonth > 0 && monthSQueue.size() >= serviceSetting.limitPerMonth)
                            monthBanLastIdx = banSchedule(list, iSched.nextIndex() - 1, monthBanLastIdx, purgeMTo);
                    }
                    if (allLimitDaily > 0 && dayAQueue.size() >= allLimitDaily)
                        dayBanLastIdx = banSchedule(list, iSched.nextIndex() - 1, dayBanLastIdx, purgeDTo);
                    if (allLimitWeekly > 0 && weekAQueue.size() >= allLimitWeekly)
                        weekBanLastIdx = banSchedule(list, iSched.nextIndex() - 1, weekBanLastIdx, purgeWTo);
                    if (allLimitMonthly > 0 && monthAQueue.size() >= allLimitMonthly)
                        monthBanLastIdx = banSchedule(list, iSched.nextIndex() - 1, monthBanLastIdx, purgeMTo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getLimitSetting(String settingAlias) {
        try {
            SettingRow allLimitWeeklyR = SettingController.getSetting(settingAlias);
            return Integer.valueOf(allLimitWeeklyR.value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int banSchedule(List<ScheduleRow> list, int fromIdx, int lastIdx, Timestamp purgeTo) {
        for (int i = fromIdx; i > lastIdx; i--) {
            ScheduleRow tmp = list.get(i);
            if (tmp.date_from.compareTo(purgeTo) < 0) {
                //System.out.println(tmp.date_from + " < " + purgeTo);
                break;
            }
            //System.out.println(tmp.date_from+" >= "+purgeTo);
            if(tmp.user_id == 0)
                tmp.user_id = 1;
        }
        return fromIdx;
    }

    private static void purgeQueueIfOlder(Deque<ScheduleRow> queue, Timestamp purgeTo) {
        while (queue.size() > 0) {
            ScheduleRow el = queue.getFirst();
            if (el.date_from.compareTo(purgeTo) <= 0) {
                //System.out.println(el.date_from+" <= "+purgeTo);
                queue.removeFirst();
            } else {
                //System.out.println(el.date_from+" > "+purgeTo);
                break;
            }
        }
    }
}
