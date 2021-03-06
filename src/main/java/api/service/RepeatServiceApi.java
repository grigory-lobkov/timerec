package api.service;

import api.session.SessionUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import controller.RepeatConvert;
import model.RepeatRow;
import model.ServiceRow;
import model.UserRow;
import storage.IMultiRowTable;
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
import java.util.List;

/**
 * Repeat actions
 *
 * RESTful: https://www.restapitutorial.com/lessons/httpmethods.html
 *
 * WARNING
 * We should not delete all records from the table - we should modify the changed one to save Schedule links, if any
 *
 */

@WebServlet(urlPatterns = "/api/repeat/service/*")
public class RepeatServiceApi extends HttpServlet {

    private IMultiRowTable<RepeatRow> storageRepeat = StorageFactory.getRepeatInstance();
    private ITable<ServiceRow> storageService = StorageFactory.getServiceInstance();

    Type listType = new TypeToken<List<RepeatRow>>() {
    }.getType();

    class Transport {
        ServiceRow service;
        List<RepeatRow> repeat;
    }

    private boolean debugLog = false;

    /**
     * GET - Read
     * 200 (OK), single customer.
     * 404 (Not Found), if ID not found or invalid.
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (debugLog) System.out.println("RepeatServiceApi.doGet(" + req.getPathInfo() + ")");

        long service_id = ServiceApi.getServiceId(req);
        Gson gson = (new GsonBuilder()).create();

        try {
            // query storage
            Transport data = new Transport();
            data.repeat = storageRepeat.select(service_id);
            data.service = storageService.select(service_id);

            if (data.repeat == null) {
                if (debugLog) System.out.println("SC_NOT_FOUND");
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                UserRow user = SessionUtils.getSessionUser(req);
                RepeatConvert.prepareOutputList(data.repeat, user);
                String jsonStr = gson.toJson(data);
                if (debugLog) System.out.println(jsonStr);
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().println(jsonStr);
            }
        } catch (Exception e) {
            if (debugLog) System.out.println("SC_NO_CONTENT");
            resp.sendError(HttpServletResponse.SC_NO_CONTENT);
            e.printStackTrace();
        }
    }

    /**
     * POST - Create
     * 201 (Created), 'Location' header with link to /customers/{id} containing new ID.
     * 404 (Not Found),
     * -409 (Conflict) if resource already exists.
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (debugLog) System.out.println("RepeatServiceApi.doPost()");

        doPut(req, resp);
    }

    /**
     * PUT - Update/Replace
     * 200 (OK) or 204 (No Content).
     * 404 (Not Found), if ID not found or invalid.
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (debugLog) System.out.println("RepeatServiceApi.doPut()");

        long service_id = ServiceApi.getServiceId(req);
        Gson gson = (new GsonBuilder()).create();
        BufferedReader rr = req.getReader();
        String line;
        boolean done1 = false;

        while ((line = rr.readLine()) != null) {
            if (debugLog) System.out.println("in: " + line);
            List<RepeatRow> datas = gson.fromJson(line, listType);
            setServiceId(datas, service_id);

            UserRow user = SessionUtils.getSessionUser(req);
            if (!RepeatConvert.checkInputList(datas, user)) {
                if (debugLog) System.out.println("SC_NOT_ACCEPTABLE");
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                return;
            }
            RepeatConvert.prepareInputList(datas, user);
            if (datas != null) {
                try {
                    if (debugLog) System.out.println("object: " + datas + "(" + datas.size() + ")");

                    // update storage
                    if (!done1) {
                        storageRepeat.delete(service_id);
                    }
                    int inserted = storageRepeat.insert(datas);

                    if (inserted == datas.size()) {
                        String jsonStr = gson.toJson(datas);
                        if (debugLog) System.out.println("out: " + jsonStr);
                        resp.setContentType("application/json; charset=UTF-8");
                        resp.setStatus(HttpServletResponse.SC_CREATED);
                        resp.getWriter().println(jsonStr);
                        done1 = true;
                    }
                } catch (Exception e) {
                    if (debugLog) System.out.println("SC_NO_CONTENT");
                    resp.sendError(HttpServletResponse.SC_NO_CONTENT);
                    done1 = true;
                    e.printStackTrace();
                }
            }
        }
        if (!done1) {
            if (debugLog) System.out.println("SC_NOT_FOUND");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void setServiceId(List<RepeatRow> datas, long service_id) {
        for (RepeatRow r : datas)
            r.service_id = service_id;
    }

    /**
     * DELETE - Delete
     * 200 (OK).
     * 404 (Not Found), if ID not found or invalid.
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (debugLog) System.out.println("RepeatServiceApi.doDelete()");

        if (debugLog) System.out.println("SC_NOT_FOUND");
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}