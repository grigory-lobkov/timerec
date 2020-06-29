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

@WebServlet(urlPatterns = "/api/register/*")
public class RegisterApi extends HttpServlet {


    private ITable<UserRow> storageUser = StorageFactory.getUserInstance();


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //UserRow user = SessionUtils.getSessionUser(req);
        Gson gson = (new GsonBuilder()).create();
        BufferedReader rr = req.getReader();
        String line;
        boolean done1 = false;

        while ((line = rr.readLine()) != null) {
            UserRow data = gson.fromJson(line, UserRow.class);
            if (data != null) {
                try {
                    // check if email is taken
                    UserRow found = storageUser.select(data.email);
                    if(found!=null) {
                        resp.setContentType("application/json; charset=UTF-8");
                        resp.getWriter().println("{\"error\":\"Email is already taken, sorry\"}");
                        resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE);
                        return;
                    }
                    // get default role
                    data.role_id = UserController.getDefaultRoleId();
                    // security purpose fields reset
                    data.name = data.name.trim();
                    data.email = data.email.trim();
                    data.user_id = 0;
                    data.owner_id = 0;
                    // integrator
                    boolean allow = Integrator.getInstance().register_allowRegistration(data);
                    // encrypt password
                    data.password = Passwords.encrypt(data.password);
                    // update storage
                    if (allow && storageUser.insert(data)) {
                        done1 = true;
                        // user auto login
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
            resp.getWriter().println("{\"success\":\"1\"}");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }


}