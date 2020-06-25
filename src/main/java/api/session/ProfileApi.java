package api.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

@WebServlet(urlPatterns = "/api/profile/*")
public class ProfileApi extends HttpServlet {


    private ITable<UserRow> storage = StorageFactory.getUserInstance();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserRow user = SessionUtils.getSessionUser(req);
        Gson gson = (new GsonBuilder()).create();

        try {
            // query storage
            UserRow data = storage.select(user.user_id);

            if (data == null) {
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


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPut(req, resp);
    }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserRow user = SessionUtils.getSessionUser(req);
        Gson gson = (new GsonBuilder()).create();
        BufferedReader rr = req.getReader();
        String line;
        boolean done1 = false;

        while ((line = rr.readLine()) != null) {
            UserRow data = gson.fromJson(line, UserRow.class);
            if (data != null) {
                try {
                    // security purpose fields reset
                    data.user_id = user.user_id;
                    data.role_id = user.role_id;
                    data.owner_id = user.owner_id;
                    data.name = data.name.trim();
                    data.email = data.email.trim();
                    boolean allow = Integrator.getInstance().profileAllowModification(user, data);
                    // password changed
                    if (data.password == null || data.password.isEmpty()) {
                        data.password = user.password;
                    } else {
                        data.password = Passwords.encrypt(data.password);
                    }
                    // update storage
                    boolean done = storage.update(data);
                    if (done) {
                        done1 = true;
                    }
                    // user auto re-login
                    if (!data.password.equals(user.password) || !data.email.equals(user.email)) {
                        SessionUtils.setResponceCookies(resp, data.email, data.password);
                        SessionUtils.createUserSession(req, data);
                    }
                } catch (Exception e) {
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
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }


    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UserRow user = SessionUtils.getSessionUser(req);
        boolean done1 = false;

        if (user != null && user.user_id > 0)
            try {
                // update storage
                done1 = storage.delete(user.user_id);

            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_NO_CONTENT);
                done1 = true;
                e.printStackTrace();
            }
        if (done1) {
            resp.getWriter().println("{}");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}