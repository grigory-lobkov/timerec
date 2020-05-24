package storage.jdbc;

import storage.Passwords;
import storage.StorageFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import java.sql.*;

public class CreateStructure implements ServletContextListener {

    private boolean debugLog = true;

    /**
     * Check, if we need to create table structure
     *
     * @return true, if need
     */
    boolean checkStructureNeedCreate() {
        boolean result = true;
        try {
            DatabaseMetaData meta = StorageFactory.connection.getMetaData();
            ResultSet res = meta.getTables("", null, "USER", new String[]{"TABLE"});
            while (res.next())
                result = false;
            res.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Initialize servlet: starts at the same beginning
     *
     * @param servletContextEvent not used
     * @throws ServletException on storage problems
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        if (debugLog) System.out.println("CreateStructure.init()");
        try {
            System.out.println("Check database...");
            if (checkStructureNeedCreate()) {
                System.out.println("Create structures...");
                Updater.createStructures();
                if (checkStructureNeedCreate()) {
                    System.out.println("ERROR: Database validation failure!");
                } else {
                    System.out.println("Database updated successfully.");
                }
            } else {
                System.out.println("Database is in normal state.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (debugLog) System.out.println("CreateStructure.init() end");
    }


    /**
     * Destruct servlet: at the most end
     *
     * Needed to close connection.
     * If not - Tomcat will not update WebServlet automatically on deploy.
     *
     * @param servletContextEvent not used
     */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        StorageFactory.dbConn.close();
    }

}

class Updater {

    /**
     * One statement for all scripts, executed here
     */
    static Statement statement;

    /**
     * Help method to beautify output and logging
     *
     * @param action log message
     * @param query string to execute by JDBC
     * @throws SQLException on storage problems
     */
    static void exec(String action, String query) throws SQLException {
        System.out.print(action + " ... ");
        statement.executeUpdate(query);
        System.out.println("ok");
    }

    /**
     * Creates table structure and indexes
     *
     * @throws SQLException on storage problems
     */
    static void createStructures() throws SQLException {
        statement = StorageFactory.connection.createStatement();

        exec("seq_tz_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_tz_id");

        exec("tz",
                "CREATE TABLE IF NOT EXISTS tz " +
                        "(tz_id BIGINT PRIMARY KEY, " +
                        "utc_offset INTEGER, " +
                        "name VARCHAR2(100))");

        exec("tz_update_list",
                "INSERT INTO tz (tz_id, utc_offset, name)" +
                        "SELECT seq_tz_id.nextval tz_id, utc_offset, name FROM (" +
                        "SELECT 2*60 utc_offset, 'Kaliningrad' name FROM dual UNION ALL " +
                        "SELECT 3*60, 'Moscow' FROM dual UNION ALL " +
                        "SELECT 4*60, 'Samara' FROM dual UNION ALL " +
                        "SELECT 5*60, 'Yekaterinburg' FROM dual UNION ALL " +
                        "SELECT 6*60, 'Omsk' FROM dual UNION ALL " +
                        "SELECT 7*60, 'Krasnoyarsk, Novosibirsk' FROM dual UNION ALL " +
                        "SELECT 8*60, 'Irkutsk' FROM dual UNION ALL " +
                        "SELECT 9*60, 'Yakutsk, Chita' FROM dual UNION ALL " +
                        "SELECT 10*60, 'Vladivostok' FROM dual UNION ALL " +
                        "SELECT 11*60, 'Magadan, Sakhalinsk, Srednekolymsk' FROM dual UNION ALL " +
                        "SELECT 12*60, 'Anadyr, Petropavlovsk-Kamchatsky' FROM dual " +
                        ")WHERE (name) NOT IN (SELECT name FROM tz)");


        exec("seq_user_id drop",
                "DROP SEQUENCE IF EXISTS seq_user_id");
        exec("user drop",
                "DROP TABLE IF EXISTS user");

        exec("seq_user_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_user_id");

        exec("user",
                "CREATE TABLE IF NOT EXISTS user " +
                        "(user_id BIGINT PRIMARY KEY, " +
                        "name VARCHAR2(1000), " +
                        "tz_id BIGINT, " +
                        "email VARCHAR2(255), " +
                        "password VARCHAR2(100))");

        exec("user_email_idx",
                "CREATE INDEX IF NOT EXISTS user_email_idx ON user (email)");

        exec("user_insert_admin",
                "INSERT INTO user (user_id, name, tz_id, email, password)" +
                        "SELECT seq_user_id.nextval, 'Admin', 2, 'admin@timerec.ru', '"+ Passwords.encrypt("admin")+"' FROM dual " +
                        "WHERE (SELECT COUNT(user_id) FROM user) = 0");

        exec("seq_service_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_service_id");

        exec("service",
                "CREATE TABLE IF NOT EXISTS service " +
                        "(service_id BIGINT PRIMARY KEY, " +
                        " name VARCHAR(1000), " +
                        " description CLOB, " +
                        " image_id BIGINT, " +
                        " duration INT, " +
                        " cost DECIMAL)");
/*
        exec("image",
                "CREATE TABLE IF NOT EXISTS image " +
                    "(image_id BIGINT PRIMARY KEY, " +
                    "filename VARCHAR2(255), " +
                    "altname VARCHAR2(255), " +
                    "wight INTEGER, " +
                    "height INTEGER, " +
                    "bitmap BLOB)");

        exec("repeat",
                "CREATE TABLE IF NOT EXISTS repeat " +
                    "(repeat_id BIGINT PRIMARY KEY, " +
                    "service_id BIGINT, " +
                    "dow INTEGER, " +
                    "duration INTEGER," +
                    "time_from TIME, " +
                    "time_to TIME, " +
                    "date_from DATE, " +
                    "date_to DATE)");

        exec("schedule",
                "CREATE TABLE IF NOT EXISTS schedule " +
                    "(schedule_id BIGINT PRIMARY KEY, " +
                    "repeat_id BIGINT, " +
                    "client_id BIGINT, " +
                    "date_from DATE, " +
                    "duration INTEGER, " +
                    "title  VARCHAR2(255), " +
                    "height INTEGER, " +
                    "description CLOB, " +
                    "bitmap BLOB)");

        exec("client",
                "CREATE TABLE IF NOT EXISTS client " +
                    "(client_id BIGINT PRIMARY KEY, " +
                    "name VARCHAR2(255), " +
                    "phone VARCHAR2(255), " +
                    "email VARCHAR2(255)," +
                    "external_ref  VARCHAR2(255))");

        exec("setting",
                "CREATE TABLE IF NOT EXISTS setting " +
                    "(setting_id BIGINT PRIMARY KEY, " +
                    "alias VARCHAR2(255), " +
                    "name VARCHAR2(1000), " +
                    "description CLOB, " +
                    "value VARCHAR2(4000), " +
                    "service_id BIGINT)");
*/
        statement.close();
    }

    /**
     * Test queries on local DB
     * @param args
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {
        Updater.createStructures();
    }

}