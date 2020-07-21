package api.service;

import api.session.SessionUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controller.ScheduleController;
import controller.UserController;
import model.ScheduleRow;
import model.UserRow;
import storage.IScheduleTable;
import storage.ITable;
import storage.StorageFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static api.service.ScheduleUtils.convertToTransport;
import static api.service.ScheduleUtils.unzone;

@WebServlet(urlPatterns = "/api/schedule/service/*")
public class ScheduleServiceApi extends HttpServlet {

    private static IScheduleTable<ScheduleRow> storageSchedule = StorageFactory.getScheduleInstance();
    private static ITable<UserRow> storageUser = StorageFactory.getUserInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //super.doGet(req, resp);
        //title, start, end, groupId
        //resp.getWriter()
        //        .println("[ { \"title\": \"All Day Event\", \"start\": \"2020-02-01\" }, { \"title\": \"Long Event\", \"start\": \"2020-02-07\", \"end\": \"2020-02-10\" }, { \"groupId\": 999, \"title\": \"Repeating Event\", \"start\": \"2020-02-09T16:00:00\" }, { \"groupId\": 999, \"title\": \"Repeating Event\", \"start\": \"2020-02-16T16:00:00\" }, { \"title\": \"Conference\", \"start\": \"2020-02-11\", \"end\": \"2020-02-13\" }, { \"title\": \"Meeting\", \"start\": \"2020-02-12T10:30:00\", \"end\": \"2020-02-12T10:42:00\" }, { \"title\": \"Lunch\", \"start\": \"2020-02-12T12:00:00\" }, { \"title\": \"Meeting\", \"start\": \"2020-02-12T12:15:00\", \"end\": \"2020-02-12T12:30:00\" }, { \"title\": \"Happy Hour\", \"start\": \"2020-02-12T17:30:00\" }, { \"title\": \"Dinner\", \"start\": \"2020-02-12T20:00:00\" }, { \"title\": \"Birthday Party\", \"start\": \"2020-02-13T07:00:00\" }, { \"title\": \"Click for Google\", \"url\": \"http://google.com/\", \"start\": \"2020-02-28\" } ]"
        //                .replace(" ", ""));
        //http://localhost:8081/timerec/api/schedule/service/1?start=2020-01-27T00%3A00%3A00%2B05%3A00&end=2020-03-09T00%3A00%3A00%2B05%3A00
        String paramStart = req.getParameter("start");
        String paramEnd = req.getParameter("end");
        ZonedDateTime dateStart = ZonedDateTime.parse(unzone(paramStart));
        ZonedDateTime dateEnd = ZonedDateTime.parse(unzone(paramEnd));
        long service_id = ServiceApi.getServiceId(req);
        UserRow user = SessionUtils.getSessionUser(req);
        List<ScheduleRow> list = ScheduleController.getScheduleByService(service_id, dateStart, dateEnd, user);

        List<ScheduleUtils.Transport> datas = new ArrayList<>(list.size());
        for (ScheduleRow row : list) {
            ScheduleUtils.Transport t = convertToTransport(row);
            if(row.user_id > 0)
                t.title = row.user_name + " - " + row.title;
            datas.add(t);
        }

        Gson gson = (new GsonBuilder()).create();
        String jsonStr = gson.toJson(datas);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().println(jsonStr);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = (new GsonBuilder()).create();
        BufferedReader rr = req.getReader();
        ScheduleRow data;
        String line;
        boolean done1 = false;
        UserRow user = SessionUtils.getSessionUser(req);

        //long service_id = ServiceApi.getServiceId(req);

        while ((line = rr.readLine()) != null) {
            data = gson.fromJson(line, ScheduleRow.class);
            try {
                ScheduleRow schedule = storageSchedule.select(data.schedule_id);
                if(schedule != null && data.service_id == schedule.service_id) {
                    UserRow client = null;
                    if(schedule.user_id > 0)
                        try {
                            client = storageUser.select(schedule.user_id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    if(ScheduleController.cancelRecord(schedule, client, user)) {
                        String jsonStr;// = "{\"success\":\"1\"}";
                        int deltaSec = UserController.getTzOffsetSeconds(user);
                        schedule.date_from = new Timestamp(schedule.date_from.getTime() + deltaSec * 1000);

                        ScheduleUtils.Transport t = convertToTransport(schedule);
                        if(schedule.user_id > 0)
                            t.title = schedule.user_name + " - " + schedule.title;
                        jsonStr = gson.toJson(t);
                        resp.setContentType("application/json; charset=UTF-8");
                        resp.getWriter().println(jsonStr);
                        done1 = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!done1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}