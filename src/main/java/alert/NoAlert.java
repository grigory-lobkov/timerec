package alert;

public class NoAlert implements IAlert {

    @Override
    public void sendAlert(String toEmail, String title, String htmlText) {
        // no need to do anything. Silent
    }

}
