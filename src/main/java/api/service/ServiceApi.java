package api.service;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

import controller.SettingController;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.ServiceRow;
import model.ServiceSettingRow;
import model.UserRow;
import storage.ITable;
import storage.StorageFactory;
import api.session.SessionUtils;

/**
 * Services actions
 *
 * RESTful: https://www.restapitutorial.com/lessons/httpmethods.html
 *
 * POST - Create
 * 201 (Created), 'Location' header with link to /api/service/{id} containing new ID.
 * 404 (Not Found),
 * -409 (Conflict) if resource already exists.
 *
 * GET - Read
 * 200 (OK), single customer.
 * 404 (Not Found), if ID not found or invalid.
 *
 * PUT - Update/Replace
 * 200 (OK) or 204 (No Content).
 * 404 (Not Found), if ID not found or invalid.
 *
 * DELETE - Delete
 * 200 (OK).
 * 404 (Not Found), if ID not found or invalid.
 */

@WebServlet(urlPatterns = "/api/service/*")
public class ServiceApi extends HttpServlet {

    private ITable<ServiceRow> storage = StorageFactory.getServiceInstance();
    private static boolean debugLog = false;

    static class Transport {
        ServiceRow service;
        ServiceSettingRow setting;
    }

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
        if (debugLog) System.out.println("ServiceApi.doGet()");

        Gson gson = (new GsonBuilder()).create();
        long service_id = getServiceId(req);

        try {
            // query storage
            ServiceRow data = storage.select(service_id);
            ServiceSettingRow setting = SettingController.getServiceSetting(service_id);

            if (data == null) {
                if (debugLog) System.out.println("SC_NOT_FOUND");
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                // check owner rights
                Long user_id = (Long) req.getAttribute("onlyIfOwner");
                if (user_id != null) {
                    if (!SessionUtils.checkOwner(resp, user_id, data.owner_id))
                        return;
                }
                String jsonStr = "{\"service\":" + gson.toJson(data) + ",\"setting\":"+gson.toJson(setting)+"}";
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

    public static long getServiceId(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.length() < 2)
            throw new RuntimeException("Identifier service_id is not set");

        path = path.substring(1);
        if (!path.matches("[0-9]*"))
            throw new RuntimeException("Identifier service_id must be numeric");

        if (debugLog) System.out.println("service_id=" + path);

        return Long.valueOf(path);
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
        if (debugLog) System.out.println("ServiceApi.doPost()");

        Gson gson = (new GsonBuilder()).create();
        UserRow user = SessionUtils.getSessionUser(req);
        BufferedReader br = req.getReader();
        String line;
        boolean done1 = false;

        while ((line = br.readLine()) != null) {
            if (debugLog) System.out.println("in: " + line);
            Transport data = gson.fromJson(line, Transport.class);
            if (data != null) {
                try {
                    if (debugLog) System.out.println("object: " + data);
                    data.service.owner_id = user.user_id;

                    // update storage
                    boolean done = storage.insert(data.service);

                    if (done && data.service.service_id > 0) {
                        SettingController.setServiceSetting(data.service.service_id, data.setting);
                        String jsonStr = gson.toJson(data);
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
        if (debugLog) System.out.println("ServiceApi.doPut()");

        Gson gson = (new GsonBuilder()).create();
        BufferedReader rr = req.getReader();
        String line;
        boolean done1 = false;

        while ((line = rr.readLine()) != null) {
            if (debugLog) System.out.println(line);
            Transport data = gson.fromJson(line, Transport.class);
            if (data != null) {
                try {
                    // check owner rights
                    Long user_id = (Long) req.getAttribute("onlyIfOwner");
                    if (user_id != null) {
                        ServiceRow dbData = storage.select(data.service.service_id);
                        if (!SessionUtils.checkOwner(resp, user_id, dbData.owner_id))
                            return;
                    }
                    // update storage
                    boolean done = storage.update(data.service);
                    SettingController.setServiceSetting(data.service.service_id, data.setting);

                    if (done) {
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
        if (done1) {
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().println("{}");
        } else {
            if (debugLog) System.out.println("SC_NOT_FOUND");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
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
        if (debugLog) System.out.println("ServiceApi.doDelete()");

        boolean done1 = false;
        long service_id = getServiceId(req);

        if (service_id > 0)
            try {
                // check owner rights
                Long user_id = (Long) req.getAttribute("onlyIfOwner");
                if (user_id != null) {
                    ServiceRow dbData = storage.select(service_id);
                    if (!SessionUtils.checkOwner(resp, user_id, dbData.owner_id))
                        return;
                }
                // update storage
                done1 = storage.delete(service_id);
                SettingController.setServiceSetting(service_id, new ServiceSettingRow());

            } catch (Exception e) {
                if (debugLog) System.out.println("SC_NO_CONTENT");
                resp.sendError(HttpServletResponse.SC_NO_CONTENT);
                done1 = true;
                e.printStackTrace();
            }
        if (done1) {
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().println("{}");
        } else {
            if (debugLog) System.out.println("SC_NOT_FOUND");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}