package controller;

import model.ScheduleRow;
import model.ServiceRow;
import model.ServiceSettingRow;
import model.UserRow;
import storage.ITable;
import storage.StorageFactory;

import java.sql.Timestamp;

public class AlertController {

    private static ITable<ServiceRow> storageService = StorageFactory.getServiceInstance();
    private static ITable<UserRow> storageUser = StorageFactory.getUserInstance();

    public static alert.IAlert mailAlert = new alert.MailAlert();

    public static void alertNewSchedule(UserRow client, ScheduleRow row, UserRow user) {
        alertSchedule("Record", client, row, user, "");
    }

    public static void alertSchedule(String action, UserRow client, ScheduleRow row, UserRow user, String bodyStart) {
        if(action==null)
            action = "";
        ServiceRow service;
        try {
            service = storageService.select(row.service_id);
            int userDeltaSec = client==null?0:UserController.getTzOffsetSeconds(client);
            UserRow owner = storageUser.select(service.owner_id);
            int ownerDeltaSec = UserController.getTzOffsetSeconds(owner);
            String userTime = (new Timestamp(row.date_from.getTime() + userDeltaSec * 1000)).toString();
            String ownerTime = (new Timestamp(row.date_from.getTime() + ownerDeltaSec * 1000)).toString();

            String titleBeforeTime = (action.isEmpty() ? "" : action + " ") + service.name + " ";

            String bodyBeforeTime = bodyStart + "<table>" +
                    (action.isEmpty() ? "" : "<tr><td>Action</td><td>"+action+"</td></tr>") +
                    "<tr><td>Name</td><td>"+htmlEntites(service.name)+"</td></tr>" +
                    "<tr><td>Description</td><td><small>"+htmlEntites(service.description)+"</small></td></tr>" +
                    "<tr><td>Duration</td><td>"+row.duration+"</td></tr>" +
                    "</table><br><table>" +
                    "<tr><td>Start time</td><td><b>";
            String bodyAfterTime = "</b></td></tr>" +
                    "<tr><td>Title</td><td>"+htmlEntites(row.title)+"</td></tr>" +
                    "<tr><td>Description</td><td>"+htmlEntites(row.description)+"</td></tr>" +
                    (client==null ? "" :
                            "<tr><td>User name</td><td>"+htmlEntites(client.name)+"</td></tr>" +
                            "<tr><td>User mail</td><td>"+htmlEntites(client.email)+"</td></tr>") +
                    (user==null ? "" : "</table><table>" +
                            "<tr><td>Initiator</td><td>"+htmlEntites(user.name)+"</td></tr>") +
                    "</table>";

            // send to user
            if(client!=null) {
                mailAlert.sendAlert(client.email,
                        titleBeforeTime + userTime,
                        bodyBeforeTime + userTime + bodyAfterTime);
            }
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

    private static String htmlEntites(String str) {
        return str.replace("&","&amp;").replace(" ","\t&nbsp;")
                .replace("<","&lt;").replace(">","&gt;")
                .replace("\n","<br>");
    }

    public static void alertCancelSchedule(UserRow client, ScheduleRow row, UserRow user) {
        alertSchedule("Cancelled", client, row, user, "");
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
