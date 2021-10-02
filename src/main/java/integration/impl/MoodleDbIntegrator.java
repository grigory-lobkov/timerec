package integration.impl;

import integration.IIntegrator;
import integration.Integrator;
import model.*;
import storage.ITable;
import storage.StorageFactory;

/**
 * MOODLE implementation of integration
 * <p>
 * https://moodle.org/
 * <p>
 * Class is singleton implemented
 * Constructor class: integration.Integrator
 */
public class MoodleDbIntegrator implements IIntegrator {

    private final ITable<TzRow> storageTz = StorageFactory.getTzInstance();
    private final ITable<IntegratorMoodleSessionsRow> storageSessions = StorageFactory.getIntegratorMoodleSessionsInstance();
    private final ITable<IntegratorMoodleUserRow> storageUser = StorageFactory.getIntegratorMoodleUserInstance();
    private boolean debugLog = false;

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
                System.out.println("Error in MoodleDbIntegrator.login_denyAutoRegister():");
                e.printStackTrace();
            }
        }
        return true;
    }

    public UserRow getUserBySessionCookie(String moodleSessionCode) throws Exception {

        IntegratorMoodleSessionsRow moodleSession = storageSessions.select(moodleSessionCode);
        if (debugLog) System.out.println("MoodleDbIntegrator.getUserBySessionCookie " + moodleSession);
        IntegratorMoodleUserRow moodleUser = storageUser.select(moodleSession.userid);
        if (debugLog) System.out.println("MoodleDbIntegrator.getUserBySessionCookie " + moodleUser);

        UserRow user = new UserRow();
        user.name = moodleUser.firstname + ' ' + moodleUser.lastname + " (" + moodleUser.username + ')';
        user.email = moodleUser.email;
        try {
            Integer offset = Integrator.getMoodleTzOffset(moodleUser.timezone);
            if (offset == null) offset = 3 * 60; // default is Moscow TZ (UTC+03)
            user.tz_utc_offset = offset;
            TzRow tz = storageTz.select(offset.toString());
            user.tz_id = tz.tz_id;
        } catch (Exception e) {
            System.out.println("Error calculating user time zone '" + moodleUser.timezone + "':");
            e.printStackTrace();
        }

        user.password = moodleSessionCode;
        if (debugLog) System.out.println("MoodleDbIntegrator.getUserBySessionCookie " + user);

        return user;
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
