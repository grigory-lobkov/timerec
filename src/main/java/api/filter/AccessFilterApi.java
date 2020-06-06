package api.filter;

import model.AccessRow;
import model.UserRow;
import storage.ITable;
import storage.StorageFactory;
import api.session.SessionUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

//@WebFilter(urlPatterns={"/service/*","/repeat/*","/schedule/*"})
//@WebFilter
@WebFilter(urlPatterns="/api/*")
public class AccessFilterApi implements Filter {

    private ITable<AccessRow> storage = StorageFactory.getAccessInstance();
    private boolean debugLog = false;
    /**
     * Map for fast right access
     * Long = role_id - user role
     * String = object_name - current page
     */
    private Map<Long, Map<String, AccessRow>> accTable;
    private Set<String> accAlways;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if(debugLog) System.out.println("AccessFilterApi.init()");
        fillAccTable();
        fillAccAlways();
    }

    private void fillAccAlways() {
        if(debugLog) System.out.println("AccessFilterApi.fillAccAlways()");
        accAlways = new HashSet<>();
        List<String> urls = Arrays.asList(
                "menu", // top menu
                "login", // manager login servlet
                "tz" // list of time zones
        );
        accAlways.addAll(urls);
    }

    private void fillAccTable() {
        if(debugLog) System.out.println("AccessFilterApi.fillAccTable()");
        accTable = new Hashtable<>();
        List<AccessRow> list;
        try {
            list = storage.select();
            for (AccessRow a:list) {
                if(debugLog) System.out.println("role_id="+a.role_id+" object="+a.object_name);
                Map<String, AccessRow> tbl = accTable.get(a.role_id);
                if(tbl==null) {
                    tbl = new Hashtable<>();
                    accTable.put(a.role_id, tbl);
                }
                tbl.put(a.object_name, a);
            }
        } catch (Exception e) {
            System.out.println("AccessFilterApi.fillAccTable(): Critical error! cannot access storage. "+e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if(debugLog) System.out.println("AccessFilterApi.doFilter("+((HttpServletRequest) servletRequest).getServletPath()+")");

        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        HttpSession session = req.getSession();

        UserRow user = (UserRow)session.getAttribute("user");
        if(user==null) {
            user = SessionUtils.createUserSessionCook(req);
        }

        boolean granted = checkRights(req, user);

        if(granted) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            if(debugLog) System.out.println("AccessFilterApi.doFilter("+user+") UNAUTHORIZED");
        }
    }


    private boolean checkRights(HttpServletRequest req, UserRow user) {
        if(debugLog) System.out.println("AccessFilterApi.checkRights("+req.getServletPath()+")");

        String action = req.getMethod();
        String page = req.getServletPath();
        int slash = page.lastIndexOf('/');
        if(slash>0)
            page = page.substring(slash+1);
        //if(debugLog) System.out.println("AccessFilterApi.checkRights action="+action+", page="+page);

        if(accAlways.contains(page)) {
            return true; // page is public
        }

        if(user!=null) {
            Map<String, AccessRow> roleRights = accTable.get(user.role_id);
            if (roleRights != null) {
                //if (debugLog) System.out.println("AccessFilterApi.checkRights roleRights.size=" + roleRights.size());
                AccessRow pageAccess = roleRights.get(page);
                if (pageAccess != null) {
                    //if (debugLog) System.out.println("AccessFilterApi.checkRights pageAccess.id=" + pageAccess.access_id);
                    boolean actionAccess = getAllActionAccess(action, pageAccess);
                    if (actionAccess) {
                        return true; // have access to all objects
                    }
                    actionAccess = getOwnActionAccess(action, pageAccess);
                    if (actionAccess) {
//                    ITable storage = StorageFactory.getInstance(page);
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
        }

        return false;
    }

//    private long getRequestObjectId(HttpServletRequest req) {
//        request = new RequestWrapper(req);
//    }

    private boolean getAllActionAccess(String action, AccessRow access) {
        if(debugLog) System.out.println("AccessFilterApi.getAllActionAccess("+action+")");
        switch(action) {
            case "GET": return access.all_get;
            case "PUT": return access.all_put;
            case "POST": return access.all_post;
            case "DELETE": return access.all_delete;
        }
        return false;
    }

    private boolean getOwnActionAccess(String action, AccessRow access) {
        if(debugLog) System.out.println("AccessFilterApi.getOwnActionAccess("+action+")");
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
        if(debugLog) System.out.println("AccessFilterApi.destroy()");
        accTable = null;
    }

}
