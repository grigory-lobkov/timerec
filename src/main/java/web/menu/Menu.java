package web.menu;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import storage.IStorage;
import storage.StorageFactory;
import web.model.Service;

@WebServlet(name="UserMenu", urlPatterns = "/menu")
public class Menu extends HttpServlet {

    private IStorage<Service> serviceStorage = StorageFactory.getServiceInstance();

    /**
     * Returns data to fill user interface main menu
     *
     * @param req  api request
     * @param resp our responce
     * @throws ServletException
     * @throws IOException storage access exception
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String jsonUser = "{\"user_id\":\"1\",\"name\":\"Валерий\"}";

        String jsonServices = "";
        try {
            // query storage
            List<Service> services = serviceStorage.selectAllQuick();

            // self generate simple json
            StringBuilder sb = new StringBuilder();
            for (Service s : services) {
                if (sb.length() > 0) sb.append(',');
                sb.append("{\"service_id\":\"" + s.service_id + "\",\"name\":\"" + s.name.replace("\\", "\\\\") + "\"}");
            }
            jsonServices = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        resp.setContentType("application/json; charset=UTF-8");
        resp.addHeader("Cache-Control", "max-age=300");
        resp.getWriter().println("{\"user\":" + jsonUser + ",\"services\":[" + jsonServices + "]}");
    }

}
