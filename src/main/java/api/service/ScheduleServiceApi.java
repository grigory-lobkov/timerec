package api.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controller.ScheduleController;
import model.ScheduleRow;
import model.UserRow;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@WebServlet(urlPatterns = "/api/schedule/service/*")
public class ScheduleServiceApi extends HttpServlet {

    class ExtendedProps {
        String client_name;
        String title;
        String description;
        int duration;
    }

    class Transport {
        // https://fullcalendar.io/docs/event-object
        long id;
        String title;
        String start;
        String end;
        String backgroundColor;
        ExtendedProps extendedProps;
    }
    //Type listType = new TypeToken<List<Transport>>() {}.getType();

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
        ZonedDateTime dateStart = ZonedDateTime.parse(paramStart);
        ZonedDateTime dateEnd = ZonedDateTime.parse(paramEnd);
        long service_id = ServiceApi.getServiceId(req, resp);
        UserRow user = getSessionUser(req);
        List<ScheduleRow> list = ScheduleController.getSchedule(service_id, dateStart, dateEnd, user);

        List<Transport> datas = new ArrayList<>(list.size());
        for (ScheduleRow row : list) {
            datas.add(convertFutureToTransport(row));
        }

        Gson gson = (new GsonBuilder()).create();
        String jsonStr = gson.toJson(datas);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().println(jsonStr);
    }

    private Transport convertFutureToTransport(ScheduleRow row) {
        ExtendedProps e = new ExtendedProps();
        e.client_name = row.client_name;
        e.title = row.title;
        e.description = row.description;
        e.duration = row.duration;

        Timestamp date_to = new Timestamp(row.date_from.getTime() + TimeUnit.MINUTES.toMillis(row.duration));

        Transport t = new Transport();
        t.id = row.schedule_id;
        if (row.client_name.isEmpty() && row.title.isEmpty()) {
            t.title = "";
        } else {
            t.title = row.client_name + " - " + row.title;
        }
        t.start = row.date_from.toString();
        t.end = date_to.toString();
        //t.backgroundColor = "#ccc";
        t.extendedProps = e;
        return t;
    }

    private UserRow getSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession();
        return (UserRow) session.getAttribute("user");
    }

}