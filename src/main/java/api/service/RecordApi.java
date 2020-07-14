package api.service;

import api.session.SessionUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import controller.ScheduleController;
import integration.Integrator;
import model.*;
import storage.ITable;
import storage.StorageFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = "/api/record/*")
public class RecordApi extends HttpServlet {

    static final int POSSIBLE_RECORD_INTERVAL = 7; // how far user can make a record

    private ITable<ServiceRow> serviceStorage = StorageFactory.getServiceInstance();
    Type repeatList = new TypeToken<List<RepeatRow>>() {
    }.getType();
    Type scheduleTList = new TypeToken<List<TransportSchedule>>() {
    }.getType();

    enum ScheduleType {

        /**
         * When client can record
         */
        SCH_FREE,

        /**
         * When schedule is busy by someone
         */
        SCH_BUSY,

        /**
         * When schedule is free, but client cannot record, because of service period limitations
         */
        SCH_BAN
    }

    class TransportSchedule {
        String start;
        int duration;
        ScheduleType type;
    }

    class TransportRecord {
        long service_id;
        String start;
        String title;
        String description;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = (new GsonBuilder()).create();
        String paramData = req.getParameter("data");

        if (paramData.equals("services")) { // data=services
            // Return list of services
            try {
                // query storage
                List<ServiceRow> data = new ArrayList<>();
                List<ServiceRow> servicesTmp = serviceStorage.select();
                for (ServiceRow s : servicesTmp) {
                    data.add(serviceStorage.select(s.service_id));
                }
                // send data
                String jsonStr = gson.toJson(data, repeatList);
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().println(jsonStr);
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_NO_CONTENT);
                e.printStackTrace();
            }

        } else if (paramData.equals("schedule")) { // data=schedule
            // Return schedule
            long service_id = getServiceId(req);
            Instant instant = Instant.now();
            //ZonedDateTime dateStart = ZonedDateTime.now();
            ZonedDateTime dateStart = instant.atZone(ZoneOffset.UTC);
            ZonedDateTime dateEnd = dateStart.plusDays(POSSIBLE_RECORD_INTERVAL);
            UserRow user = SessionUtils.getSessionUser(req);
            // get data
            List<ScheduleRow> list = ScheduleController.getScheduleByService(service_id, dateStart, dateEnd, user);
            // Check for service restrictions
            ScheduleController.checkScheduleLimits(list, service_id, dateStart, dateEnd, user);
            // Convert to transport
            List<TransportSchedule> data = new ArrayList<>(list.size());
            for (ScheduleRow row : list) {
                data.add(convertToTransport(row));
            }
            // send data
            String jsonStr = gson.toJson(data, scheduleTList);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().println(jsonStr);
        }
    }

    private TransportSchedule convertToTransport(ScheduleRow row) {
        TransportSchedule t = new TransportSchedule();

        //t.start = row.date_from.toString();
        //DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        //t.start = df.format(row.date_from);
        t.start = row.date_from.toString().replace(" ", "T");

        t.type = row.user_id > 0 ? (row.user_id == 1 ? ScheduleType.SCH_BAN : ScheduleType.SCH_BUSY) : ScheduleType.SCH_FREE;
        t.duration = row.duration;

        return t;
    }

    public static long getServiceId(HttpServletRequest req) {
        String path = req.getParameter("service_id");
        if (path == null || path.length() < 1)
            throw new RuntimeException("Parameter SERVICE_ID is not set");

        if (!path.matches("[0-9]*"))
            throw new RuntimeException("Parameter SERVICE_ID must be numeric");

        return Long.valueOf(path);
    }


    /**
     * Insert of new record (row in Schedule)
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = (new GsonBuilder()).create();
        BufferedReader rr = req.getReader();
        TransportRecord data = null;
        String line;

        while ((line = rr.readLine()) != null) {
            //description.append(line);
            data = gson.fromJson(line, TransportRecord.class);
        }

        if (data == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }


        long service_id = data.service_id;
        ZonedDateTime dateStart = ZonedDateTime.parse(data.start + "Z").minusDays(1);
        ZonedDateTime dateEnd = dateStart.plusDays(2);
        UserRow user = SessionUtils.getSessionUser(req);
        // get schedule
        List<ScheduleRow> list = ScheduleController.getScheduleByService(service_id, dateStart, dateEnd, user);
        // Check for service restrictions
        ScheduleController.checkScheduleLimits(list, service_id, dateStart, dateEnd, user);

        // search given parameters in schedule
        boolean done1 = false;
        Timestamp srch = Timestamp.valueOf(data.start.replace("T", " "));
        for (ScheduleRow row : list) {
            if (srch.equals(row.date_from)) {
                try {
                    row.title = data.title;
                    row.description = data.description;
                    if (row.user_id > 0) {
                        resp.setContentType("application/json; charset=UTF-8");
                        resp.getWriter().println(row.user_id == 1 ? "{\"ban\":\"1\"}" : "{\"busy\":\"1\"}");
                        done1 = true;
                    } else {
                        boolean allow = Integrator.getInstance().record_allowRecord(user, row);
                        if (allow && ScheduleController.makeRecord(row, user, user)) {
                            resp.setContentType("application/json; charset=UTF-8");
                            resp.getWriter().println("{\"success\":\"1\"}");
                            done1 = true;
                        }
                    }
                } catch (Exception e) {
                    resp.sendError(HttpServletResponse.SC_NO_CONTENT);
                    done1 = true;
                    e.printStackTrace();
                }
            }
        }
        if (!done1) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}