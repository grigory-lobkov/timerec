package api.menu;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import api.session.SessionUtils;
import model.AccessRow;
import model.UserRow;
import storage.ITable;
import storage.StorageFactory;
import model.ServiceRow;

@WebServlet(urlPatterns = "/api/menu")
public class MenuApi extends HttpServlet {

    private final int MAX_SERVICE_NAME_LENGTH = 25; // maximum length in dropdown menu

    private ITable<ServiceRow> serviceStorage = StorageFactory.getServiceInstance();
    private ITable<AccessRow> accessStorage = StorageFactory.getAccessInstance();

    private Set<String> servicePages; // pages to add known service_id
    //private Set<String> userPages; // user menu pages
    private Set<String> otherPages; // pages without parameters

    private Map<Long, List<Page>> roleMenu;
    private Map<String, String> pageNames; // user menu pages

    class Page {
        String item;
        String name;
        boolean isService;
        boolean isOther;
        //boolean isUser;
        AccessRow access;

        @Override
        public String toString() {
            return "Page{" +
                    "item='" + item + '\'' +
                    ", name='" + name + '\'' +
                    ", isService=" + isService +
                    ", isOther=" + isOther +
                    ", access=" + access +
                    '}';
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        roleMenu = null;
        otherPages = null;
        //userPages = null;
        servicePages = null;
    }

    @Override
    public void init() throws ServletException {
        servicePages = new HashSet<>(Arrays.asList("service", "repeat", "schedule"));
        //userPages = new HashSet<>(Arrays.asList("login", "register", "profile", "setting", "user"));
        otherPages = new HashSet<>(Arrays.asList("login", "register", "service", "setting", "record", "records"));
        pageNames = new Hashtable<>();
        pageNames.put("login", "Login");
        pageNames.put("service", "Service");
        pageNames.put("repeat", "Repeat");
        pageNames.put("schedule", "Schedule");
        pageNames.put("register", "Register");
        pageNames.put("setting", "Settings");
        pageNames.put("record", "Record");
        pageNames.put("records", "Record list");

        roleMenu = new Hashtable<>();
        List<AccessRow> list;
        try {
            list = accessStorage.select();
            for (AccessRow a : list) {
                List<Page> tbl = roleMenu.get(a.role_id);
                if (tbl == null) {
                    tbl = new ArrayList<>();
                    roleMenu.put(a.role_id, tbl);
                }
                Page t = genMenuItem(a);
                if (t != null)
                    tbl.add(t);
            }
        } catch (Exception e) {
            System.out.println("MenuApi.init(): Critical error! cannot access storage. " + e.getMessage());
            e.printStackTrace();
        }
        super.init();
    }

    private Page genMenuItem(AccessRow a) {
        Page t = null;
        Boolean isS = servicePages.contains(a.object_name);
        Boolean isO = otherPages.contains(a.object_name);
        //Boolean isU = otherPages.contains(a.object_name);
        if (isS || isO) {
            t = new Page();
            t.item = a.object_name;
            t.name = pageNames.get(a.object_name);
            if (t.name == null || t.name.isEmpty())
                t.name = a.object_name;
            t.isService = isS;
            t.isOther = isO;
            //t.isUser = isU;
            t.access = a;
        }
        return t;
    }

    /**
     * Returns data to fill user interface main menu
     *
     * @param req  api request
     * @param resp our responce
     * @throws ServletException
     * @throws IOException      storage access exception
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long service_id = getServiceId(req);
        UserRow user = SessionUtils.getSessionUser(req);
        if (user == null)
            user = SessionUtils.getPublicUser();

        String jsonUser = "{\"user_id\":\"" + user.user_id + "\",\"name\":\"" + user.name + "\"}";
        String jsonServices = "";
        String jsonPages = "";
        if (user.user_id > 0)
            try {
                // query storage
                List<ServiceRow> services = serviceStorage.select();
                // services list
                jsonServices = genServicesJson(services);
            } catch (Exception e) {
                e.printStackTrace();
            }
        //if (service_id <= 0 && services.size() > 0) service_id = services.get(0).service_id;
        jsonPages = genPagesJson(user, service_id);

        resp.setContentType("application/json; charset=UTF-8");
        resp.addHeader("Cache-Control", "max-age=300");
        resp.getWriter().println("{\"user\":" + jsonUser +
                ",\"services\":[" + jsonServices + "]" +
                ",\"pages\":[" + jsonPages + "]" +
                "}");
    }

    /**
     * Generate json PAGES
     *
     * @param user
     * @param service_id
     * @return
     */
    private String genPagesJson(UserRow user, long service_id) {
        //System.out.println("genPagesJson(service_id="+service_id+")");
        StringBuilder result = new StringBuilder();
        boolean showService = service_id > 0;
        String serviceParam = showService ? "service_id=" + service_id : "";

        List<Page> pages = roleMenu.get(user.role_id);
        for (Page p : pages) {
            if (p.isOther || showService) {
                if (result.length() > 0)
                    result.append(',');
                result.append("{\"item\":\"" + p.item +
                        "\",\"name\":\"" + p.name +
                        "\",\"param\":\"" + (p.isService ? serviceParam : "") +
                        "\"}");
            }
        }
        return result.toString();
    }

    /**
     * Generate json SERVICES
     *
     * @param services
     * @return
     */
    private String genServicesJson(List<ServiceRow> services) {
        StringBuilder result = new StringBuilder();
        for (ServiceRow s : services) {
            if (result.length() > 0)
                result.append(',');
            result.append("{\"service_id\":\"" + s.service_id +
                    "\",\"name\":\"" + s.name.substring(0, MAX_SERVICE_NAME_LENGTH).replace("\\", "\\\\") +
                    "\"}");
        }
        return result.toString();
    }

    public static long getServiceId(HttpServletRequest req) {
        String path = req.getParameter("service_id");

        if (path == null || path.length() < 1) return 0;

        if (!path.matches("[0-9]*")) return 0;

        return Long.valueOf(path);
    }
}