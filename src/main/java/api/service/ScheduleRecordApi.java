package api.service;

import api.session.SessionUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controller.ScheduleController;
import controller.UserController;
import model.ScheduleRow;
import model.UserRow;
import storage.IScheduleTable;
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

@WebServlet(urlPatterns = "/api/schedule/records")
public class ScheduleRecordApi extends HttpServlet {

    private static IScheduleTable<ScheduleRow> storageSchedule = StorageFactory.getScheduleInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //http://localhost:8081/timerec/api/schedule/service/1?start=2020-01-27T00%3A00%3A00%2B05%3A00&end=2020-03-09T00%3A00%3A00%2B05%3A00
        String paramStart = req.getParameter("start");
        String paramEnd = req.getParameter("end");
        ZonedDateTime dateStart = ZonedDateTime.parse(unzone(paramStart));
        ZonedDateTime dateEnd = ZonedDateTime.parse(unzone(paramEnd));
        UserRow user = SessionUtils.getSessionUser(req);
        List<ScheduleRow> list = ScheduleController.getScheduleByUser(dateStart, dateEnd, user);

        List<ScheduleUtils.Transport> datas = new ArrayList<>(list.size());
        for (ScheduleRow row : list) {
            ScheduleUtils.Transport t = convertToTransport(row);
            if(row.user_id > 0)
                t.title = row.service_name + " - " + row.title;
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
        int deltaSec = UserController.getTzOffsetSeconds(user);
        Timestamp timeNow = new Timestamp(System.currentTimeMillis() - deltaSec * 1000);

        while ((line = rr.readLine()) != null) {
            data = gson.fromJson(line, ScheduleRow.class);
            try {
                ScheduleRow schedule = storageSchedule.select(data.schedule_id);
                if(schedule != null && schedule.user_id == user.user_id) {
                    if(timeNow.compareTo(schedule.date_from) > 0) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    } else if(ScheduleController.cancelRecord(schedule, user, user)) {
                        resp.setContentType("application/json; charset=UTF-8");
                        resp.getWriter().println("{\"success\":\"1\"}");
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