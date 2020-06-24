package integration.impl;

import integration.IIntegrator;
import model.ScheduleRow;
import model.UserRow;

/**
 * MOODLE implementation of integration
 *
 * https://moodle.org/
 *
 * Class is singleton implemented
 * Constructor class: integration.Integrator
 */
public class MoodleIntegrator implements IIntegrator {

    /**
     * Initialization block
     */
    {

    }


    /**
     * Allow user to be registered while logon
     * If user not found, it can be registered automatically
     * Procedure will get only {@code email} and {@code password}
     * and can fill other fields to create new user (role_id, name, e.t.c.)
     *
     * {@code user.password} is not encrypted
     *
     * @param user
     * @return {@code true} to deny
     */
    public boolean loginDenyAutoRegister(UserRow user){
        return true;
    }


    /**
     * Allow registered user to be logged in
     * It can be denied even if user is registered
     *
     * {@code user.password} is encrypted
     *
     * @param user
     * @return {@code true} to accept
     */
    public boolean loginAllowRegistered(UserRow user){
        return true;
    }


    /**
     * Allow new user to be registered
     * Procedure can change any field to store in database (role_id, name, e.t.c.)
     *
     * {@code user.password} is not encrypted
     *
     * @param user
     * @return {@code true} to accept
     */
    public boolean registerAllowRegistration(UserRow user){
        return true;
    }


    /**
     * Allow user to make session
     * User can log-in several months ago. This method can prevent it.
     *
     * {@code user.password} is encrypted
     *
     * @param user
     * @return {@code true} to accept
     */
    public boolean sessionAllowUser(UserRow user){
        return true;
    }


    /**
     * Allow user to make a record
     *
     * {@code user.password} is encrypted
     *
     * @param user
     * @param schedule
     * @return {@code true} to accept
     */
    public boolean recordAllowRecord(UserRow user, ScheduleRow schedule){
        return true;
    }


    /**
     * Allow user to modify own profile
     * If {@code oldUser.password} not equals to {@code newUser.password}, then
     * {@code newUser.password} is not encrypted and contains a new password
     *
     * If {@code newUser.password==""}, then password is not changed
     *
     * @param oldUser
     * @param newUser
     * @return {@code true} to accept
     */
    public boolean profileAllowModification(UserRow oldUser, UserRow newUser){
        return true;
    }

}
