package controller;

import model.RepeatRow;
import model.UserRow;

import java.util.List;

public class RepeatConvert {

    public static final int MIN_TIME = 0; // minimum seconds
    public static final int TIME_IN_DAY = 60 * 60 * 24; // seconds in day
    public static final int MAX_TIME = TIME_IN_DAY; // maximum seconds


    public static boolean checkInputList(List<RepeatRow> datas, UserRow user) {
        for(RepeatRow r: datas) {
            if(r.time_from<RepeatConvert.MIN_TIME || r.time_from>RepeatConvert.MAX_TIME)
                return false;
            if(r.time_to<RepeatConvert.MIN_TIME || r.time_to>RepeatConvert.MAX_TIME)
                return false;
        }
        return true;
    }


    public static void prepareInputList(List<RepeatRow> datas, UserRow user) {
        int delta = UserController.getTzOffsetSeconds(user);
        for(RepeatRow r: datas) {
            r.time_from -= delta;
            r.time_to -= delta;
            if(r.time_from<0) {
                r.dow = (r.dow + 6) % 7; // decrease day of week
                r.time_from += TIME_IN_DAY;
                r.time_to += TIME_IN_DAY;
            }
            if(r.time_from>0) {
                r.dow = (r.dow + 1) % 7; // increase day of week
                r.time_from -= TIME_IN_DAY;
                r.time_to -= TIME_IN_DAY;
            }
        }
    }

}
