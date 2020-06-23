package controller;

import model.*;
import storage.ITable;
import storage.StorageFactory;

import java.sql.Timestamp;

public class AlertController {

    private static ITable<ServiceRow> storageService = StorageFactory.getServiceInstance();
    private static ITable<UserRow> storageUser = StorageFactory.getUserInstance();

    public static alert.IAlert mailAlert = new alert.MailAlert();

    public static void alertNewSchedule(UserRow user, ScheduleRow row) {
        ServiceRow service;
        int userDeltaSec = UserController.getTzOffsetSeconds(user);
        try {
            service = storageService.select(row.service_id);
            UserRow owner = storageUser.select(service.owner_id);
            int ownerDeltaSec = UserController.getTzOffsetSeconds(owner);

            String titleBeforeTime = service.name + " ";

            String bodyBeforeTime = "<table>" +
                    "<tr><td>Name</td><td>"+service.name+"</td></tr>" +
                    "<tr><td>Description</td><td><small>"+service.description+"</small></td></tr>" +
                    "<tr><td>Duration</td><td>"+row.duration+"</td></tr>" +
                    "<tr><td>Start time</td><td><b>";
            String bodyAfterTime = "</b></td></tr>" +
                    "<tr><td>Title</td><td>"+row.title+"</td></tr>" +
                    "<tr><td>Description</td><td>"+row.description+"</td></tr>" +
                    "<tr><td>User name</td><td>"+user.name+"</td></tr>" +
                    "<tr><td>User mail</td><td>"+user.email+"</td></tr>" +
                    "</table>";
            String userTime = (new Timestamp(row.date_from.getTime() + userDeltaSec * 1000)).toString();
            String ownerTime = (new Timestamp(row.date_from.getTime() + ownerDeltaSec * 1000)).toString();

            // send to user
            mailAlert.sendAlert(user.email,
                    titleBeforeTime + userTime,
                    bodyBeforeTime + userTime + bodyAfterTime);

            // send to administrator
            ServiceSettingRow settings = SettingController.getServiceSetting(row.service_id);
            if(settings.alertPaths.isEmpty()) {
                if(owner!=null)
                    mailAlert.sendAlert(owner.email,
                            titleBeforeTime + ownerTime,
                            bodyBeforeTime + ownerTime + bodyAfterTime);
            } else {
                alertByPaths(settings.alertPaths,
                        titleBeforeTime + ownerTime,
                        bodyBeforeTime + ownerTime + bodyAfterTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void alertByPaths(String alertPaths, String title, String htmlText) {
        String[] aPaths = alertPaths.split("[;,]");
        for (String path:aPaths) {
            String[] aPath = path.split("[:]");
            if(aPath.length==2) {
                String pathType = aPath[0].trim().toLowerCase();
                String pathAdress = aPath[1].trim();
                if(!pathType.isEmpty() && !pathAdress.isEmpty()) {
                    switch (pathType) {
                        case "email":
                        case "e-mail":
                        case "mail":
                            mailAlert.sendAlert(pathAdress, title, htmlText);
                            break;
                        default:
                            System.out.println("Unknown alert type '"+path.trim()+"'");
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        alertByPaths("email:grigorymail@mail.ru, telegram:89221223282; skype:grigory_asb", "3333", "555555555555555");
    }
}
