package api;

import api.setting.SettingUtils;
import storage.CreateStructure;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Date;

@WebListener
public class Initializer implements ServletContextListener {

    ServletContextListener storageInit = new CreateStructure();
    ServletContextListener settingInit = new SettingUtils();

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("\n" + new Date());
        storageInit.contextInitialized(servletContextEvent);
        settingInit.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        settingInit.contextDestroyed(servletContextEvent);
        storageInit.contextDestroyed(servletContextEvent);
    }

}