package api.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import controller.UserController;
import integration.Integrator;
import model.UserRow;
import storage.ITable;
import storage.Passwords;
import storage.StorageFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(urlPatterns = "/api/login/*")
public class LoginApi extends HttpServlet {

    private ITable<UserRow> storage = StorageFactory.getUserInstance();
    private boolean debugLog = false;

    /**
     * Login attempt
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (debugLog) System.out.println("LoginApi.doPost()");

        Gson gson = (new GsonBuilder()).create();
        BufferedReader br = req.getReader();
        String line;
        boolean done1 = false;

        while ((line = br.readLine()) != null) {
            if (debugLog) System.out.println("LoginApi in: " + line);
            UserRow data = gson.fromJson(line, UserRow.class);
            if (data != null) {
                try {
                    if (debugLog) System.out.println("LoginApi object: " + data);
                    data.email = data.email.trim();

                    // look for storage
                    UserRow dbUser = SessionUtils.checkAndGetUser(data.email, data.password);
                    String jsonStr;
                    if (dbUser == null) {
                        dbUser = tryAutoRegister(data, req, resp);
                    }

                    jsonStr = "{\"success\":\"0\"}";
                    if (dbUser != null) {
                        if (Integrator.instance().login_allowRegistered(dbUser)) {
                            jsonStr = "{\"success\":\"1\",\"user\":" + gson.toJson(dbUser) + "}";
                            resp.setStatus(HttpServletResponse.SC_CREATED);
                            SessionUtils.setResponceCookies(resp, dbUser.email, dbUser.password);
                            SessionUtils.createUserSession(req, dbUser);
                            if (debugLog) System.out.println("LoginApi logged in "+ dbUser.name+" ("+dbUser.email+")");
                        } else {
                            if (debugLog) System.out.println("LoginApi SC_FORBIDDEN");
                            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                        }
                    } else {
                        if (debugLog) System.out.println("LoginApi SC_NO_CONTENT");
                        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                    resp.getWriter().println(jsonStr);
                    if (debugLog) System.out.println("LoginApi out: " + jsonStr);
                    done1 = true;
                } catch (Exception e) {
                    if (debugLog) System.out.println("LoginApi SC_NO_CONTENT");
                    resp.sendError(HttpServletResponse.SC_NO_CONTENT);
                    done1 = true;
                    e.printStackTrace();
                }
            }
        }
        if (!done1) {
            if (debugLog) System.out.println("LoginApi SC_NOT_FOUND");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    private UserRow tryAutoRegister(UserRow data, HttpServletRequest req, HttpServletResponse resp) {
        if (debugLog) System.out.println("LoginApi.tryAutoRegister("+data+")");
        data.owner_id = 0;
        data.role_id = 0;
        boolean autoReg = Integrator.instance().login_denyAutoRegister(data);
        if (debugLog) System.out.println("LoginApi.tryAutoRegister login_denyAutoRegister="+autoReg+" "+data);
        if (!autoReg) {
            data.user_id = 0;
            if (data.name == null || data.name.isEmpty()) {
                String[] s = data.email.split("@");
                data.name = s[0].substring(0, 1).toUpperCase() + s[0].substring(1);
                data.name = data.name.trim();
            }
            if (data.role_id == 0) {
                data.role_id = UserController.getDefaultRoleId();
            }
            String savePassword = data.password;
            try {
                if (debugLog) System.out.println("LoginApi.tryAutoRegister new "+data);
                data.password = Passwords.encrypt(data.password);
                // check storage for already registered account
                UserRow dbUser = storage.select(data.email);
                if (dbUser == null) {
                    // update storage
                    if (storage.insert(data)) {
                        if (debugLog) System.out.println("LoginApi.tryAutoRegister inserted.");
                        return data;
                    }
                } else {
                    data.user_id = dbUser.user_id;
                    if (!dbUser.name.equals(data.name) || dbUser.role_id != data.role_id || dbUser.tz_id != data.tz_id ||
                            !dbUser.email.equals(data.email) || !dbUser.password.equals(data.password) ||
                            dbUser.image_id != data.image_id || dbUser.owner_id != data.owner_id) {
                        // update storage
                        storage.update(data);
                        if (debugLog) System.out.println("LoginApi.tryAutoRegister updated.");
                    }
                    return data;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                data.password = savePassword;
            }
        }
        return null;
    }

}
