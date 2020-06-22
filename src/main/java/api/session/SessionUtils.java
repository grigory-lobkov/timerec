package api.session;

import model.UserRow;
import storage.ITable;
import storage.Passwords;
import storage.StorageFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SessionUtils {

    static public final int COOKIE_SESSION_LIFETIME = 60 * 60 * 24 * 30; // set user and password cookies lifetime in milliseconds

    static private ITable<UserRow> storage = StorageFactory.getUserInstance();

    static private boolean debugLog = false;


    /**
     * Returns {@code model.UserRow}, found by {@code email} and {@code password} or {@code null}
     *
     * @param email    user email
     * @param password user password to check
     * @return {@code model.UserRow} if {@code email} and {@code password} is correct
     */
    static public UserRow checkAndGetUser(String email, String password) {
        if (debugLog) System.out.println("SessionUtils.checkAndGetUser()");
        try {
            UserRow dbUser = storage.select(email);
            if (dbUser != null) {
                String gotPass = Passwords.encrypt(password);
                boolean success = password.equals(dbUser.password) || gotPass.equals(dbUser.password);
                if (success) {
                    if (debugLog)
                        System.out.println("SessionUtils.checkAndGetUser(" + (email != null ? email : "") + "," + (password != null ? password : "") + ") success.");
                    return dbUser;
                } else {
                    if (debugLog)
                        System.out.println("SessionUtils.checkAndGetUser(" + (email != null ? email : "") + "," + (password != null ? password : "") + ") failure.");
                    return null;
                }
            }
        } catch (Exception e) {
            if (debugLog)
                System.out.println("SessionUtils.checkAndGetUser(" + (email != null ? email : "") + "," + (password != null ? password : "") + ") not found in USER table.");
        }
        return null;
    }


    /**
     * Add authorizing Cookie information to {@code resp}
     *
     * @param resp     user response
     * @param email
     * @param password
     */
    static void setResponceCookies(HttpServletResponse resp, String email, String password) {
        if (debugLog) System.out.println("SessionUtils.setResponceCookies()");
        Cookie cookie = new Cookie("email", email);
        cookie.setMaxAge(COOKIE_SESSION_LIFETIME);
        cookie.setPath("/");
        resp.addCookie(cookie);
        cookie = new Cookie("password", password);
        cookie.setMaxAge(COOKIE_SESSION_LIFETIME);
        cookie.setPath("/");
        resp.addCookie(cookie);
    }


    /**
     * Add {@code model.UserRow} to {@code req} {@code HttpSession}
     *
     * @param req  user request
     * @param user information to save in session
     */
    static public void createUserSession(HttpServletRequest req, UserRow user) {
        if (debugLog) System.out.println("SessionUtils.createUserSession()");
        HttpSession session = req.getSession();
        session.setAttribute("user", user);
    }


    /**
     * Deletes session and cookies info
     *
     * @param req servlet request
     * @param resp servlet responce
     * @return
     */
    static public void deleteUserSessionCook(HttpServletRequest req, HttpServletResponse resp) {
        if (debugLog) System.out.println("SessionUtils.deleteUserSessionCook()");
        HttpSession session = req.getSession(false);
        if(session!=null) {
            session.setAttribute("user", null);
            Cookie[] cookies = req.getCookies();
            for (Cookie c : cookies) {
                String n = c.getName();
                if (n.equals("email") || n.equals("password") || n.equals("JSESSIONID")) {
                    if (debugLog) System.out.println("SessionUtils.deleteUserSessionCook() " + n);
                    c.setValue("");
                    c.setMaxAge(0);
                    resp.addCookie(c);
                    c = new Cookie(n, "");
                    c.setMaxAge(0);
                    c.setPath("/");
                    resp.addCookie(c);
                }
            }
            session.invalidate();
        }
    }

    /**
     * Add {@code model.UserRow} to {@code req} {@code HttpSession} if cookies contains valid {@code email} and {@code password}
     *
     * @param req user request
     * @return authorized {@code model.UserRow}
     */
    static public UserRow createUserSessionCook(HttpServletRequest req) {
        if (debugLog) System.out.println("SessionUtils.createUserSessionCook()");
        UserRow user = null;
        Cookie[] cookies = req.getCookies();
        String email = null;
        String password = null;
        if(cookies!=null)
            for (Cookie c : cookies) {
                String n = c.getName();
                if (debugLog) System.out.println("SessionUtils.createUserSessionCook() Cook '"+n+"'='"+c.getValue()+"'");
                if (n.equals("email")) email = c.getValue();
                else if (n.equals("password")) password = c.getValue();
            }
        if (email != null && password != null) {
            user = SessionUtils.checkAndGetUser(email, password);
            if (user != null)
                createUserSession(req, user);
        }
        return user;
    }


    /**
     * Check if user is owner of object
     * Sends error SC_UNAUTHORIZED to user response {@code resp}
     *
     * @param resp            user response
     * @param session_user_id current {@code user_id} (from session parameters)
     * @param object_owner_id current object {@code owner_id}
     * @return true if user equals to owner
     */
    public static boolean checkOwner(HttpServletResponse resp, Long session_user_id, long object_owner_id) {
        if (debugLog) System.out.println("SessionUtils.checkOwner()");

        if (session_user_id == object_owner_id) {
            return true;
        }

        System.out.println("SessionUtils.checkOwner() user=" + session_user_id + " no access, because owner_id=" + object_owner_id);
        try {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (IOException e) {
        }
        return false;
    }


    /**
     * Default user, when user is not logged in
     *
     * @return
     */
    public static UserRow getPublicUser() {
        UserRow u = new UserRow();
        u.role_id = 1; // CreateStructure.Updater.userRole() MUST set public role_id = 1
        u.name = "";
        u.user_id = -1;
        return u;
    }


    /**
     * Gets {@code model.UserRow} from session
     *
     * @param req user request
     * @return current session user {@code model.UserRow}
     */
    public static UserRow getSessionUser(HttpServletRequest req) {
        HttpSession session = req.getSession();
        UserRow user = (UserRow) session.getAttribute("user");
        return user;
    }



}
