package alert;

import controller.SettingController;
import model.SettingRow;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class MailAlert implements IAlert {

    private final String SETTING_MAIL_SERV = "MAIL_ALERT_SERV";
    private final String SETTING_MAIL_USER = "MAIL_ALERT_USER";
    private final String SETTING_MAIL_PASS = "MAIL_ALERT_PASS";
    private final String SETTING_MAIL_FROM = "MAIL_ALERT_FROM";

    private String emailFrom;

    private String authServ;

    private String authUser;

    private String authPass;

    private boolean validSettings;

    {
        validSettings = true;
        authServ = getSettingValue(SETTING_MAIL_SERV, "Alert Email server name", "Email server address");
        authUser = getSettingValue(SETTING_MAIL_USER, "Alert Email user name", "Email user for authentication");
        authPass = getSettingValue(SETTING_MAIL_PASS, "Alert Email password", "Email password for authentication");
        emailFrom = getSettingValue(SETTING_MAIL_FROM, "Alert Email From", "Email From address");
    }

    private String getSettingValue(String alias, String defName, String defDesc) {
        SettingRow s = SettingController.getSetting(alias);
        if (s == null || s.value.isEmpty()) {
            System.out.println("MailAlert.getSettingValue() setting '" + alias + "' is not set!");
            validSettings = false;
            if (s == null) {
                s = new SettingRow();
                s.value = "";
                s.alias = alias;
                s.name = defName;
                s.description = defDesc;
                SettingController.setSetting(s);
            }
        }
        return s.value;
    }

    @Override
    public void sendAlert(String toEmail, String title, String htmlText) {
        if (!validSettings) {
            System.out.println("MailAlert.sendAlert() Cannot send email, because not all settings set.");
            return;
        }
        System.out.println("MailAlert.sendAlert(" + toEmail + ")");
        if (!toEmail.contains("@")) {
            System.out.println("Recipient email '" + toEmail + "' does not contain domain name");
            return;
        }
        Properties props = new Properties();
        props.put("mail.smtp.host", authServ);
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        javax.mail.Session session = javax.mail.Session.getDefaultInstance(props);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("TimeRec: " + title, "UTF-8");

            String html = "<html><head><style type='text/css'>" +
                    "table,th,td{border:1px solid gray;border-collapse:collapse}" +
                    "th,td{padding:0.3em}" +
                    "</style></head>" +
                    "<body><h1>" + title + "</h1>" +
                    "<p>" + htmlText + " </p>" +
                    "<small>This messege was sent by TimeRec</small>" +
                    "</body></html>";

            Multipart multipart = new MimeMultipart();
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(html, "text/html; charset=utf-8");
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            Transport.send(message, authUser, authPass);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
