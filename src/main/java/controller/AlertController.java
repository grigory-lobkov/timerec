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

            String title = service.name + " " + row.date_from;

            String tBeforeTime = "<table>" +
                    "<tr><td>Name</td><td>"+service.name+"</td></tr>" +
                    "<tr><td>Description</td><td><small>"+service.description+"</small></td></tr>" +
                    "<tr><td>Duration</td><td>"+row.duration+"</td></tr>" +
                    "<tr><td>Start time</td><td><b>";
            String tAfterTime = "</b></td></tr>" +
                    "<tr><td>Title</td><td>"+row.title+"</td></tr>" +
                    "<tr><td>Description</td><td>"+row.description+"</td></tr>" +
                    "<tr><td>User name</td><td>"+user.name+"</td></tr>" +
                    "<tr><td>User mail</td><td>"+user.email+"</td></tr>" +
                    "</table>";
            String userTime = (new Timestamp(row.date_from.getTime() + userDeltaSec * 1000)).toString();
            String ownerTime = (new Timestamp(row.date_from.getTime() + ownerDeltaSec * 1000)).toString();

            // send to user
            mailAlert.sendAlert(user.email, title , tBeforeTime + userTime + tAfterTime);

            // send to administrator
            ServiceSettingRow settings = SettingController.getServiceSetting(row.service_id);
            if(settings.alertPaths.isEmpty()) {
                if(owner!=null)
                    mailAlert.sendAlert(owner.email, title , tBeforeTime + ownerTime + tAfterTime);
            } else {
                alertByPaths(settings.alertPaths, title , tBeforeTime + ownerTime + tAfterTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void alertByPaths(String alertPaths, String title, String htmlText) {
        String[] l = alertPaths.split("[;,]");
        for (String e:l) {
            String[] p = e.split("[:]");
            if(p.length==2) {
                String t = p[0].trim().toLowerCase();
                String a = p[1].trim();
                if(!t.isEmpty() && !a.isEmpty()) {
                    switch (t) {
                        case "email":
                        case "e-mail":
                        case "mail":
                            mailAlert.sendAlert(a, title, htmlText);
                            break;
                        default:
                            System.out.println("Unknown alert type '"+e.trim()+"'");
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        alertByPaths("email:grigorymail@mail.ru, telegram:89221223282; skype:grigory_asb", "3333", "555555555555555");
    }
}
