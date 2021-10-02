package integration.impl;

import integration.IIntegrator;
import integration.Integrator;
import model.ScheduleRow;
import model.TzRow;
import model.UserRow;
import storage.ITable;
import storage.StorageFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * MOODLE implementation of integration
 * <p>
 * https://moodle.org/
 * <p>
 * Class is singleton implemented
 * Constructor class: integration.Integrator
 */
public class MoodleIntegrator implements IIntegrator {

    private static final String USER_PROFILE_URL = Integrator.getProperty("INTEGRATOR_MOODLE_USER_PROFILE_URL");
    private final ITable<TzRow> storageTz = StorageFactory.getTzInstance();

    static {
        if (USER_PROFILE_URL == null)
            throw new RuntimeException("Environment variable INTEGRATOR_MOODLE_USER_PROFILE_URL is not set");
    }


    /**
     * Allow user to be registered while logon
     * If user not found, it can be registered automatically
     * Procedure will get only {@code email} and {@code password}
     * and can fill other fields to create new user (role_id, name, e.t.c.)
     * <p>
     * {@code user.password} is not encrypted
     * <p>
     * Integrator expects {@code email} is equals to "MoodleSession" and
     * {@code password} have Moodle session cookie
     * <p>
     * Событие возникает на странице "login". Определяет, разрешено ли
     * пользователю зарегистрироваться при авторизации.
     * {@code true} - не разрешено
     * {@code false} - разрешено
     * Инверсия "deny" сделана для того, чтобы "не вникая" все методы
     * интегратора по-умолчанию возвращали "true"
     *
     * @param user who is doing action
     * @return {@code true} to deny
     */
    public boolean login_denyAutoRegister(UserRow user) {
        if (user.email != null && user.email.equals("MoodleSession") &&
                user.password != null && user.password.length() > 10) {
            try {
                UserRow moodleUser = getUserBySessionCookie(user.password);
                moodleUser.copyTo(user);
                return false;
            } catch (Exception e) {
                System.out.println("Error in MoodleIntegrator.login_denyAutoRegister():");
                e.printStackTrace();
            }
        }
        return true;
    }

    public UserRow getUserBySessionCookie(String moodleSession) throws IOException {
        URL url = new URL(USER_PROFILE_URL);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setDoOutput(true);
        con.setRequestProperty("Cookie", "MoodleSession=" + moodleSession);
        con.connect();

        UserRow user = new UserRow();
        String id = "";
        String firstName = "";
        String lastName = "";
        user.email = "";
        user.tz_id = 0;

        if (con.getResponseCode() == 200) {
            String inKey = "input type=\"text\"";
            int fromInLine = 0; // how far from input
            String idKey = "name=\"id\" value=\"";
            String unKey = "value=\"";
            String opKey = "option value=\"";
            String selKey = "\" selected";
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String strCurrentLine;
            String strPrevLine = null;
            while ((strCurrentLine = br.readLine()) != null) {
                fromInLine++;
                if (strCurrentLine.contains(idKey)) {
                    id = strCurrentLine.substring(strCurrentLine.indexOf(idKey) + idKey.length());
                    id = id.substring(0, id.indexOf("\""));
                } else if (strCurrentLine.contains(inKey)) {
                    fromInLine = 0;
                } else if (fromInLine < 10 && strCurrentLine.contains(unKey)) {
                    // get user email, name
                    String value = strCurrentLine.substring(strCurrentLine.indexOf(unKey) + unKey.length());
                    value = value.substring(0, value.indexOf("\""));
                    if (strPrevLine.contains("id=\"id_email\"")) {
                        user.email = value;
                    } else if (strPrevLine.contains("id=\"id_firstname\"")) {
                        firstName = value;
                    } else if (strPrevLine.contains("id=\"id_lastname\"")) {
                        lastName = value;
                    }
                } else if (strCurrentLine.contains(selKey) && strCurrentLine.contains(opKey)) {
                    // get user Timezone
                    String value = strCurrentLine.substring(strCurrentLine.indexOf(opKey) + opKey.length());
                    value = value.substring(0, value.indexOf("\""));
                    Integer offset = Integrator.getMoodleTzOffset(value);
                    if (offset != null)
                        updateUserTz(user, offset);
                }
                strPrevLine = strCurrentLine;
            }
        }

        if (id.isEmpty())
            throw new RuntimeException("Cannot find USER_ID");

        if (user.email.isEmpty())
            throw new RuntimeException("Cannot find user ID=" + id + " email!");

        if (user.tz_id == 0)
            updateUserTz(user, (03) * 60); // default is Moscow TZ (UTC+03)

        user.name = firstName + ' ' + lastName;
        user.password = moodleSession;

        return user;
    }

    private void updateUserTz(UserRow user, Integer offset) {
        try {
            TzRow tz = storageTz.select(offset.toString());
            user.tz_id = tz.tz_id;
            user.tz_utc_offset = offset;
        } catch (Exception e) {
            System.out.println("Error in MoodleDbIntegrator.updateUserTz():");
            e.printStackTrace();
        }
    }

    /**
     * Allow registered user to be logged in
     * It can be denied even if user is registered
     * <p>
     * {@code user.password} is encrypted
     * <p>
     * Событие возникает на странице "login". Определяет, разрешено ли
     * зарегистрированному пользователю выполнить авторизацию.
     * {@code true} - разрешено
     * {@code false} - запрещено
     *
     * @param user who is doing action
     * @return {@code true} to accept
     */
    public boolean login_allowRegistered(UserRow user) {
        return true;
    }


    /**
     * Allow new user to be registered
     * Procedure can change any field to store in database (role_id, name, e.t.c.)
     * <p>
     * {@code user.password} is not encrypted
     * <p>
     * Событие возникает на странице "register". Определяет, разрешено ли
     * данному пользователю пройти регистрацию (можно разрешить регистрацию
     * только e-mail из списка).
     * {@code true} - разрешено
     * {@code false} - запрещено
     *
     * @param user who is doing action
     * @return {@code true} to accept
     */
    public boolean register_allowRegistration(UserRow user) {
        return true;
    }


    /**
     * Allow user to make session
     * User can log-in several months ago. This method can prevent it.
     * <p>
     * {@code user.password} is encrypted
     * <p>
     * Определяет, может ли пользователь по сохраненным в кукис логину/паролю
     * создать новую сессию и начать работать с системой
     * {@code true} - может
     * {@code false} - не может
     *
     * @param user who is doing action
     * @return {@code true} to accept
     */
    public boolean session_allowUser(UserRow user) {
        return true;
    }


    /**
     * Allow user to make a record
     * <p>
     * {@code user.password} is encrypted
     * <p>
     * Событие возникает на странице "record", где пользователь делает запись
     * в расписание. Метод определяет, может ли данный пользователь {@code user}
     * сделать запись {@code schedule}.
     * {@code true} - может
     * {@code false} - не может
     *
     * @param user     who is doing action
     * @param schedule chosen time and other data inside
     * @return {@code true} to accept
     */
    public boolean record_allowRecord(UserRow user, ScheduleRow schedule) {
        return true;
    }


    /**
     * Allow user to modify own profile
     * If {@code oldUser.password} not equals to {@code newUser.password}, then
     * {@code newUser.password} is not encrypted and contains a new password
     * <p>
     * If {@code newUser.password==""}, then password is not changed
     * <p>
     * Событие возникает на странице "profile". Определяет, может ли пользователь
     * внести данную правку в свой профиль.
     * {@code true} - может
     * {@code false} - не может
     * <p>
     * Если {@code newUser.password} пустой, значит пользователь пароль не меняет.
     * Если {@code oldUser.password} не равен {@code newUser.password}, значит
     * {@code newUser.password} не зашифрован и хранит новый пароль пользователя.
     *
     * @param oldUser old user data
     * @param newUser new user data
     * @return {@code true} to accept
     */
    public boolean profile_allowModification(UserRow oldUser, UserRow newUser) {
        return true;
    }

}
