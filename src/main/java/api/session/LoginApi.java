package api.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.UserRow;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(urlPatterns = "/api/login/*")
public class LoginApi extends HttpServlet {

    //private ITable<UserRow> storage = StorageFactory.getUserInstance();
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

                    // look for storage
                    UserRow dbUser = SessionUtils.checkAndGetUser(data.email, data.password);
                    String jsonStr;

                    if (dbUser != null) {
                        jsonStr = "{\"success\":\"1\",\"user\":" + gson.toJson(dbUser) + "}";
                        resp.setStatus(HttpServletResponse.SC_CREATED);
                        SessionUtils.setResponceCookies(resp, dbUser.email, dbUser.password);
                        SessionUtils.createUserSession(req, dbUser);
                    } else {
                        jsonStr = "{\"success\":\"0\"}";
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


}
