package web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class Initializer implements ServletContextListener {

    ServletContextListener storageInit = new storage.jdbc.CreateStructure();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        storageInit.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        storageInit.contextInitialized(servletContextEvent);
    }

}
