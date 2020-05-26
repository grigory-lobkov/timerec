package web.filter;

import storage.IStorage;
import storage.StorageFactory;
import model.Access;
import model.User;
import web.session.SessionUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

@WebFilter
public class AccessFilter implements Filter {

    private IStorage<Access> storage = StorageFactory.getAccessInstance();
    private boolean debugLog = true;
    /**
     * Map for fast right access
     * Long = role_id - user role
     * String = object_name - current page
     */
    private Map<Long, Map<String,Access>> accTable;
    private Set<String> accAlways;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if(debugLog) System.out.println("AccessFilter.init()");
        fillAccTable();
        fillAccAlways();
    }

    private void fillAccAlways() {
        if(debugLog) System.out.println("AccessFilter.fillAccAlways()");
        accAlways = new HashSet<>();
        List<String> urls = Arrays.asList(
                "login", // manager login servlet
                "tz" // list of time zones
        );
        accAlways.addAll(urls);
    }

    private void fillAccTable() {
        if(debugLog) System.out.println("AccessFilter.fillAccTable()");
        accTable = new Hashtable<>();
        List<Access> list;
        try {
            list = storage.selectAllQuick();
            for (Access a:list) {
                Map<String, Access> tbl = accTable.get(a.role_id);
                if(tbl==null) {
                    tbl = new Hashtable<>();
                    accTable.put(a.role_id, tbl);
                }
                tbl.put(a.object_name, a);
            }
        } catch (Exception e) {
            System.out.println("AccessFilter.fillAccTable(): Critical error! cannot access storage. "+e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if(debugLog) System.out.println("AccessFilter.doFilter()");

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletRequest;
        HttpSession session = req.getSession();

        User user = (User)session.getAttribute("user");
        if(user==null) {
            user = SessionUtils.createUserSessionCook(req);
        }

        boolean granted = checkRights(req, user);

        if(granted) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }


    private boolean checkRights(HttpServletRequest req, User user) {
        if(debugLog) System.out.println("AccessFilter.checkRights()");

        String page = req.getContextPath();
        String action = req.getMethod();

        if(accAlways.contains(page)) {
            return true; // page is public
        }

        Map<String, Access> roleRights = accTable.get(action);
        if(roleRights != null) {
            Access pageAccess = roleRights.get(page);
            if(pageAccess!=null) {
                boolean actionAccess = getAllActionAccess(action, pageAccess);
                if(actionAccess) {
                    return true; // everybody have access
                }
                actionAccess = getOwnActionAccess(action, pageAccess);
                if(actionAccess) {
//                    IStorage storage = StorageFactory.getInstance(page);
//                    long object_id = getRequestObjectId(req);
//                    if(object_id>=0) {
//                        try {
//                            boolean isOwner = storage.checkIsOwner(object_id, user.user_id);
//                            if (isOwner) {
//                                return true; // have access as owner
//                            }
//                        } catch (SQLException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    req.setAttribute("onlyIfOwner", user.user_id);
                }
            }
        }

        return false;
    }

//    private long getRequestObjectId(HttpServletRequest req) {
//        request = new RequestWrapper(req);
//    }

    private boolean getAllActionAccess(String action, Access access) {
        if(debugLog) System.out.println("AccessFilter.getAllActionAccess("+action+")");
        switch(action) {
            case "GET": return access.all_get;
            case "PUT": return access.all_put;
            case "POST": return access.all_post;
            case "DELETE": return access.all_delete;
        }
        return false;
    }

    private boolean getOwnActionAccess(String action, Access access) {
        if(debugLog) System.out.println("AccessFilter.getOwnActionAccess("+action+")");
        switch(action) {
            case "GET": return access.own_get;
            case "PUT": return access.own_put;
            case "POST": return access.own_post;
            case "DELETE": return access.own_delete;
        }
        return false;
    }

    @Override
    public void destroy() {
        if(debugLog) System.out.println("AccessFilter.destroy()");
        accTable = null;
    }

}
