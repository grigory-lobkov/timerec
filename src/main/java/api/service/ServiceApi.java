package api.service;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.ServiceRow;
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
        if (debugLog) System.out.println("ServiceApi.doGet()");

        String path = req.getPathInfo();
        if (path == null || path.length() < 2
                || !path.substring(1).matches("[0-9]*"))
            return;

        Gson gson = (new GsonBuilder()).create();
        if (debugLog) System.out.println("service_id=" + path.substring(1));

        long service_id = Long.valueOf(path.substring(1));

        try {
            // query storage
            ServiceRow data = storage.select(service_id);

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
        if (debugLog) System.out.println("ServiceApi.doPost()");

        Gson gson = (new GsonBuilder()).create();
        BufferedReader br = req.getReader();
        String line;
        boolean done1 = false;

        while ((line = br.readLine()) != null) {
            if (debugLog) System.out.println("in: " + line);
            ServiceRow data = gson.fromJson(line, ServiceRow.class);
            if (data != null) {
                try {
                    if (debugLog) System.out.println("object: " + data);
                    data.owner_id = SessionUtils.getSessionUserId(req);

                    // update storage
                    boolean done = storage.insert(data);

                    if (done && data.service_id > 0) {
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
            ServiceRow data = gson.fromJson(line, ServiceRow.class);
            if (data != null) {
                try {
                    // check owner rights
                    Long user_id = (Long) req.getAttribute("onlyIfOwner");
                    if (user_id != null) {
                        ServiceRow dbData = storage.select(data.service_id);
                        if (!SessionUtils.checkOwner(resp, user_id, dbData.owner_id))
                            return;
                    }
                    // update storage
                    boolean done = storage.update(data);

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

        String path = req.getPathInfo();
        if (path == null) return;
        boolean done1 = false;
        if (debugLog) System.out.println("service_id=" + path.substring(1));

        long service_id = Long.valueOf(path.substring(1));

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