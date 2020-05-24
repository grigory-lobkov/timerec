package web.session;

import storage.IStorage;
import storage.StorageFactory;
import web.model.User;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

@WebServlet(urlPatterns = "/login/*")
public class Login extends HttpServlet {

    private IStorage<User> storage = StorageFactory.getUserInstance();
    private boolean debugLog = true;

}
