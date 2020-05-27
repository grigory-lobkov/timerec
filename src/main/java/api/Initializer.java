package api;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Date;

@WebListener
public class Initializer implements ServletContextListener {

    ServletContextListener storageInit = new storage.jdbc.CreateStructure();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("\n" + new Date());
        storageInit.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        storageInit.contextDestroyed(servletContextEvent);
    }

}