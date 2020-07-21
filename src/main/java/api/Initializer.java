package api;

import controller.SettingController;
import storage.CreateStructure;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Date;
import java.util.TimeZone;

@WebListener
public class Initializer implements ServletContextListener {

    ServletContextListener storageInit = new CreateStructure();
    ServletContextListener settingInit = new SettingController();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("\n" + new Date());
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        storageInit.contextInitialized(servletContextEvent);
        settingInit.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        settingInit.contextDestroyed(servletContextEvent);
        storageInit.contextDestroyed(servletContextEvent);
    }

}