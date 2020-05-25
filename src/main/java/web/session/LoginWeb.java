package web.session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import storage.IStorage;
import storage.Passwords;
import storage.StorageFactory;
import web.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(urlPatterns = "/login/*")
public class LoginWeb extends HttpServlet {

    private IStorage<User> storage = StorageFactory.getUserInstance();
    private boolean debugLog = true;

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
        if (debugLog) System.out.println("LoginWeb.doPost()");

        Gson gson = (new GsonBuilder()).create();
        BufferedReader br = req.getReader();
        String line;
        boolean done1 = false;

        while ((line = br.readLine()) != null) {
            if (debugLog) System.out.println("in: " + line);
            User data = gson.fromJson(line, User.class);
            if (data != null) {
                try {
                    if (debugLog) System.out.println("object: " + data);

                    // look for storage
                    User dbUser = getUser(data.email, data.password);
                    String jsonStr;

                    if (dbUser != null) {
                        jsonStr = gson.toJson(dbUser);
                        jsonStr = "{success:1,user:" + jsonStr + "}";
                        resp.setStatus(HttpServletResponse.SC_CREATED);
                    } else {
                        jsonStr = "{success:0}";
                        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    }
                    resp.getWriter().println(jsonStr);
                    if (debugLog) System.out.println("out: " + jsonStr);
                    done1 = true;
                } catch (Exception e) {
                    if (debugLog) System.out.println("SC_NO_CONTENT");
                    resp.sendError(HttpServletResponse.SC_NO_CONTENT);
                    done1 = true;
                    e.printStackTrace();
                }
            }
        }
        if (!done1) {
            if (debugLog) System.out.println("SC_NOT_FOUND");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public User getUser(String email, String password) {
        try {
            User dbUser = storage.select(email);
            if(dbUser!=null) {
                String gotPass = Passwords.encrypt(password);
                boolean success = password.equals(dbUser.password) || gotPass.equals(dbUser.password);
                if(success) {
                    if (debugLog) System.out.println("LoginWeb.getUser("+(email!=null?email:"")+","+(password!=null?password:"")+") success.");
                    return dbUser;
                } else {
                    if (debugLog) System.out.println("LoginWeb.getUser("+(email!=null?email:"")+","+(password!=null?password:"")+") failure.");
                    return null;
                }
            }
        } catch (Exception e) {
            if (debugLog) System.out.println("LoginWeb.getUser("+(email!=null?email:"")+","+(password!=null?password:"")+") not found in USER table.");
        }
        return null;
    }

}
