package api.session;

import storage.IStorage;
import storage.Passwords;
import storage.StorageFactory;
import model.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class SessionUtils {

    static public final int COOKIE_SESSION_LIFETIME = 60 * 60 * 24 * 30; // set user and password cookies lifetime in milliseconds

    static private IStorage<User> storage = StorageFactory.getUserInstance();
    static private boolean debugLog = true;


    /**
     * Returns {@code model.User}, found by {@code email} and {@code password} or {@code null}
     *
     * @param email    user email
     * @param password user password to check
     * @return {@code model.User} if {@code email} and {@code password} is correct
     */
    static public User checkAndGetUser(String email, String password) {
        if (debugLog) System.out.println("SessionUtils.checkAndGetUser()");
        try {
            User dbUser = storage.select(email);
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
        resp.addCookie(cookie);
        cookie = new Cookie("password", password);
        cookie.setMaxAge(COOKIE_SESSION_LIFETIME);
        resp.addCookie(cookie);
    }


    /**
     * Add {@code model.User} to {@code req} {@code HttpSession}
     *
     * @param req  user request
     * @param user information to save in session
     */
    static public void createUserSession(HttpServletRequest req, User user) {
        if (debugLog) System.out.println("SessionUtils.createUserSession()");
        HttpSession session = req.getSession();
        session.setAttribute("user", user);
    }


    /**
     * Add {@code model.User} to {@code req} {@code HttpSession} if cookies contains valid {@code email} and {@code password}
     *
     * @param req user request
     * @return authorized {@code model.User}
     */
    static public User createUserSessionCook(HttpServletRequest req) {
        if (debugLog) System.out.println("SessionUtils.createUserSessionCook()");
        User user = null;
        Cookie[] cookies = req.getCookies();
        String email = null;
        String password = null;
        for (Cookie c : cookies) {
            String n = c.getName();
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
     * Gets {@code model.User} {@code user_id} from session
     *
     * @param req user request
     * @return current session {@code model.User.user_id}
     */
    public static long getSessionUserId(HttpServletRequest req) {

        HttpSession session = req.getSession();

        User user = (User) session.getAttribute("user");

        return user == null ? -1 : user.user_id;
    }

}
