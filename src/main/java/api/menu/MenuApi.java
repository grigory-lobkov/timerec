package api.menu;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

import model.UserRow;
import storage.ITable;
import storage.StorageFactory;
import model.ServiceRow;

@WebServlet(urlPatterns = "/api/menu")
public class MenuApi extends HttpServlet {

    private ITable<ServiceRow> serviceStorage = StorageFactory.getServiceInstance();

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
        HttpSession session = req.getSession();
        UserRow user = (UserRow) session.getAttribute("user");

        // anonymous set
        String jsonUser = "{\"user_id\":\"-1\",\"name\":\"\"}";
        StringBuilder jsonServices = new StringBuilder();
        if(user!=null) {
            // authorized user
            jsonUser = "{\"user_id\":\"" + user.user_id + "\",\"name\":\"" + user.name + "\"}";
            try {
                // query storage
                List<ServiceRow> services = serviceStorage.select();

                // self generate simple json
                //StringBuilder jsonServices = new StringBuilder();
                for (ServiceRow s : services) {
                    if (jsonServices.length() > 0) jsonServices.append(',');
                    jsonServices.append("{\"service_id\":\"" + s.service_id + "\",\"name\":\"" + s.name.replace("\\", "\\\\") + "\"}");
                }
                //jsonServices = jsonServices.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        resp.setContentType("application/json; charset=UTF-8");
        resp.addHeader("Cache-Control", "max-age=300");
        resp.getWriter().println("{\"user\":" + jsonUser + ",\"services\":[" + jsonServices + "]}");
    }

}
