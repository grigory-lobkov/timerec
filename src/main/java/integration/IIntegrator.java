package integration;

import model.ScheduleRow;
import model.UserRow;


/**
 * Implementation is singleton class
 * Constructor class: integration.Integrator
 */
public interface IIntegrator {


    /**
     * Allow user to be registered while logon
     * If user not found, it can be registered automatically
     * Procedure will get only {@code email} and {@code password}
     * and can fill other fields to create new user (role_id, name, e.t.c.)
     *
     * {@code user.password} is not encrypted
     *
     * Событие возникает на странице "login". Определяет, разрешено ли
     * пользователю зарегистрироваться при авторизации.
     * {@code true} - не разрешено
     * {@code false} - разрешено
     * Инверсия "deny" делана для того, чтобы "не вникая" все методы
     * интегратора по-умолчанию возвращали "true"
     *
     * @param user who is doing action
     * @return {@code true} to deny
     */
    boolean login_denyAutoRegister(UserRow user);


    /**
     * Allow registered user to be logged in
     * It can be denied even if user is registered
     *
     * {@code user.password} is encrypted
     *
     * Событие возникает на странице "login". Определяет, разрешено ли
     * зарегистрированному пользователю выполнить авторизацию.
     * {@code true} - разрешено
     * {@code false} - запрещено
     *
     * @param user who is doing action
     * @return {@code true} to accept
     */
    boolean login_allowRegistered(UserRow user);


    /**
     * Allow new user to be registered
     * Procedure can change any field to store in database (role_id, name, e.t.c.)
     *
     * {@code user.password} is not encrypted
     *
     * Событие возникает на странице "register". Определяет, разрешено ли
     * данному пользователю пройти регистрацию (можно разрешить регистрацию
     * только e-mail из списка).
     * {@code true} - разрешено
     * {@code false} - запрещено
     *
     * @param user who is doing action
     * @return {@code true} to accept
     */
    boolean register_allowRegistration(UserRow user);


    /**
     * Allow user to make session
     * User can log-in several months ago. This method can prevent it.
     *
     * {@code user.password} is encrypted
     *
     * Определяет, может ли пользователь по сохраненным в кукис логину/паролю
     * создать новую сессию и начать работать с системой
     * {@code true} - может
     * {@code false} - не может
     *
     * @param user who is doing action
     * @return {@code true} to accept
     */
    boolean session_allowUser(UserRow user);


    /**
     * Allow user to make a record
     *
     * {@code user.password} is encrypted
     *
     * Событие возникает на странице "record", где пользователь делает запись
     * в расписание. Метод определяет, может ли данный пользователь {@code user}
     * сделать запись {@code schedule}.
     * {@code true} - может
     * {@code false} - не может
     *
     * @param user who is doing action
     * @param schedule chosen time and other data inside
     * @return {@code true} to accept
     */
    boolean record_allowRecord(UserRow user, ScheduleRow schedule);


    /**
     * Allow user to modify own profile
     * If {@code oldUser.password} not equals to {@code newUser.password}, then
     * {@code newUser.password} is not encrypted and contains a new password
     *
     * If {@code newUser.password==""}, then password is not changed
     *
     * Событие возникает на странице "profile". Определяет, может ли пользователь
     * внести данную правку в свой профиль.
     * {@code true} - может
     * {@code false} - не может
     *
     * Если {@code newUser.password} пустой, значит пользователь пароль не меняет.
     * Если {@code oldUser.password} не равен {@code newUser.password}, значит
     * {@code newUser.password} не зашифрован и хранит новый пароль пользователя.
     *
     * @param oldUser old user data
     * @param newUser new user data
     * @return {@code true} to accept
     */
    boolean profile_allowModification(UserRow oldUser, UserRow newUser);

}
