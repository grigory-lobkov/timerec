package api.duty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import controller.SettingController;
import model.SettingRow;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Settings actions
 *
 */

@WebServlet(urlPatterns = "/api/setting/*")
public class SettingApi extends HttpServlet {

    Type listType = new TypeToken<List<SettingRow>>() {
    }.getType();

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
        if (debugLog) System.out.println("SettingApi.doGet()");

        Gson gson = (new GsonBuilder()).create();

        try {
            // get settings
            List<SettingRow> data = SettingController.getSettings();
            // sort settings
            Comparator<SettingRow> compareByAlias = Comparator.comparing((SettingRow o) -> o.alias);
            Collections.sort(data, compareByAlias);

            if (debugLog) for (SettingRow d : data) System.out.println(d);

            if (data == null || data.size() == 0) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                String jsonStr = gson.toJson(data);
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().println(jsonStr);
            }
        } catch (Exception e) {
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
        if (debugLog) System.out.println("SettingApi.doPost()");

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
        if (debugLog) System.out.println("SettingApi.doPut()");

        Gson gson = (new GsonBuilder()).create();
        BufferedReader rr = req.getReader();
        String line;
        boolean done1 = false;

        while ((line = rr.readLine()) != null) {
            List<SettingRow> datas = gson.fromJson(line, listType);

            if (datas != null && datas.size() > 0) {
                for (SettingRow data : datas) {
                    SettingRow setting = SettingController.getSetting(data.alias);
                    if (setting != null && (setting.value == null && data.value != null
                            || setting.value != null && data.value != null && !setting.value.equals(data.value))) {
                        setting.value = data.value;
                        SettingController.setSetting(setting);
                        if (debugLog) System.out.println("new setting=" + setting);
                        done1 = true;
                    }
                }
            }
        }
        if (done1) {
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().println("{\"success\":\"1\"}");
            if (debugLog) System.out.println("SettingApi.doPut() OK");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            if (debugLog) System.out.println("SettingApi.doPut() SC_NOT_FOUND");
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
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}