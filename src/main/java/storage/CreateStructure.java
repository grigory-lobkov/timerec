package storage;

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
            DatabaseMetaData meta = StorageFactory.dbConn.connection().getMetaData();
            ResultSet res = meta.getTables("", null, "REPEAT", new String[]{"TABLE"});
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
        if (debugLog) System.out.println("CreateStructure.contextInitialized()");
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
        if (debugLog) System.out.println("CreateStructure.contextInitialized() end");
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
        if (debugLog) System.out.println("CreateStructure.contextDestroyed()");
        StorageFactory.dbConn.close();
    }

}

class Updater {

    static final String ROLE_ADMIN = "Administrators";
    static final String ROLE_OPER = "Operators";
    static final String ADMIN_NAME = "Admin";
    static final String ADMIN_EMAIL = "admin@timerec.ru";
    static final String ADMIN_PASSWORD = "admin";

    /**
     * One statement for all scripts, executed here
     */
    static Statement statement;

    /**
     * Help method to beautify output and logging
     *
     * @param action log message
     * @param query  string to execute by JDBC
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
        Connection conn = StorageFactory.dbConn.connection();
        statement = conn.createStatement();

        exec("seq_image_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_image_id");

        exec("image",
                "CREATE TABLE IF NOT EXISTS image " +
                        "(image_id BIGINT PRIMARY KEY, " +
                        " filename VARCHAR2(100)," +
                        " altname VARCHAR2(1000)," +
                        " width INTEGER," +
                        " height INTEGER," +
                        " bitmap BLOB," +
                        " owner_id BIGINT)");

        exec("seq_tz_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_tz_id");

        exec("tz",
                "CREATE TABLE IF NOT EXISTS tz " +
                        "(tz_id BIGINT PRIMARY KEY," +
                        " utc_offset INTEGER," +
                        " name VARCHAR2(1000)," +
                        " owner_id BIGINT)");

        exec("tz_update_list",
                "INSERT INTO tz (tz_id, utc_offset, name)" +
                        "SELECT seq_tz_id.nextval tz_id, utc_offset, name FROM (" +
                        " SELECT 2*60 utc_offset, 'Kaliningrad' name FROM dual UNION ALL" +
                        " SELECT 3*60, 'Moscow' FROM dual UNION ALL" +
                        " SELECT 4*60, 'Samara' FROM dual UNION ALL" +
                        " SELECT 5*60, 'Yekaterinburg' FROM dual UNION ALL" +
                        " SELECT 6*60, 'Omsk' FROM dual UNION ALL" +
                        " SELECT 7*60, 'Krasnoyarsk, Novosibirsk' FROM dual UNION ALL" +
                        " SELECT 8*60, 'Irkutsk' FROM dual UNION ALL" +
                        " SELECT 9*60, 'Yakutsk, Chita' FROM dual UNION ALL" +
                        " SELECT 10*60, 'Vladivostok' FROM dual UNION ALL" +
                        " SELECT 11*60, 'Magadan, Sakhalinsk, Srednekolymsk' FROM dual UNION ALL" +
                        " SELECT 12*60, 'Anadyr, Petropavlovsk-Kamchatsky' FROM dual" +
                        ")WHERE (name) NOT IN (SELECT name FROM tz)");

        userAndRights();

        serviceSchedule();

/*
        exec("client",
                "CREATE TABLE IF NOT EXISTS client " +
                        "(client_id BIGINT PRIMARY KEY, " +
                        " name VARCHAR2(1000)," +
                        " phone VARCHAR2(100)," +
                        " email VARCHAR2(100)," +
                        " external_ref VARCHAR2(1000))");

        exec("setting",
                "CREATE TABLE IF NOT EXISTS setting " +
                        "(setting_id BIGINT PRIMARY KEY, " +
                        " alias VARCHAR2(64), " +
                        " name VARCHAR2(1000), " +
                        " description CLOB, " +
                        " value VARCHAR2(4000), " +
                        " service_id BIGINT," +
                        " owner_id BIGINT)");
*/
        statement.close();
        conn.close();
    }

    /**
     * ROLE, ACCESS, USER tables creation
     *
     * @throws SQLException
     */
    private static void userAndRights() throws SQLException {

        exec("seq_role_id drop", "DROP SEQUENCE IF EXISTS seq_role_id");
        exec("role drop", "DROP TABLE IF EXISTS role");
        exec("seq_role_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_role_id");
        exec("role",
                "CREATE TABLE IF NOT EXISTS role " +
                        "(role_id BIGINT PRIMARY KEY," +
                        " name VARCHAR2(1000)," +
                        " owner_id BIGINT)");
        exec("role_update_list",
                "INSERT INTO role (role_id, name)" +
                        "SELECT seq_role_id.nextval role_id, name FROM (" +
                        " SELECT '"+ROLE_ADMIN+"' name FROM dual UNION ALL" +
                        " SELECT '"+ROLE_OPER+"' name FROM dual" +
                        ")WHERE (name) NOT IN (SELECT name FROM role)");

        exec("seq_access_id drop", "DROP SEQUENCE IF EXISTS seq_access_id");
        exec("access drop", "DROP TABLE IF EXISTS access");
        exec("seq_access_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_access_id");
        exec("access",
                "CREATE TABLE IF NOT EXISTS access " +
                        "(access_id BIGINT PRIMARY KEY," +
                        " role_id BIGINT," +
                        " object_name VARCHAR2(100)," +
                        " all_get BOOLEAN," +
                        " all_put BOOLEAN," +
                        " all_post BOOLEAN," +
                        " all_delete BOOLEAN," +
                        " own_get BOOLEAN," +
                        " own_put BOOLEAN," +
                        " own_post BOOLEAN," +
                        " own_delete BOOLEAN,)");


        exec("seq_user_id drop", "DROP SEQUENCE IF EXISTS seq_user_id");
        exec("user drop", "DROP TABLE IF EXISTS user");
        exec("seq_user_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_user_id");
        exec("user",
                "CREATE TABLE IF NOT EXISTS user " +
                        "(user_id BIGINT PRIMARY KEY," +
                        " role_id BIGINT," +
                        " name VARCHAR2(1000)," +
                        " tz_id BIGINT," +
                        " email VARCHAR2(100)," +
                        " password VARCHAR2(100)," +
                        " image_id BIGINT," +
                        " owner_id BIGINT)");

        exec("user_email_idx",
                "CREATE INDEX IF NOT EXISTS user_email_idx ON user (email)");


        exec("user_insert_admin",
                "INSERT INTO user (user_id, role_id, name, tz_id, email, password)" +
                        "SELECT seq_user_id.nextval, role_id, '"+ADMIN_NAME+"', 2, '"+ADMIN_EMAIL+"', '" + Passwords.encrypt(ADMIN_PASSWORD) + "'" +
                        "FROM role " +
                        "WHERE name = '"+ROLE_ADMIN+"' AND (SELECT COUNT(user_id) FROM user) = 0");

        exec("access_insert_admin",
                "INSERT INTO access (access_id, role_id, object_name," +
                        " all_get, all_put, all_post, all_delete, own_get, own_put, own_post, own_delete)" +
                        "SELECT seq_access_id.nextval, role_id, page.name, true, true, true, true, true, true, true, true " +
                        "FROM user, (" +
                        " SELECT 'login' name FROM dual UNION ALL" +
                        " SELECT 'menu' FROM dual UNION ALL" +
                        " SELECT 'setting' FROM dual UNION ALL" +
                        " SELECT 'user' FROM dual UNION ALL" +
                        " SELECT 'role' FROM dual UNION ALL" +
                        " SELECT 'access' FROM dual UNION ALL" +
                        " SELECT 'image' FROM dual UNION ALL" +
                        " SELECT 'service' FROM dual UNION ALL" +
                        " SELECT 'repeat' FROM dual UNION ALL" +
                        " SELECT 'schedule' FROM dual UNION ALL" +
                        " SELECT 'client' FROM dual UNION ALL" +
                        " SELECT 'tz' FROM dual" +
                        ") page " +
                        "WHERE email = '"+ADMIN_EMAIL+"' AND 0 = (" +
                        " SELECT COUNT(access_id) FROM access " +
                        " WHERE role_id = user.role_id AND object_name = page.name)");
    }

    /**
     * SERVICE, REPEAT, SCHEDULE tables creation
     *
     * @throws SQLException
     */
    private static void serviceSchedule() throws SQLException {

        exec("seq_service_id drop", "DROP SEQUENCE IF EXISTS seq_service_id");
        exec("service drop", "DROP TABLE IF EXISTS service");
        exec("seq_service_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_service_id");

        exec("service",
                "CREATE TABLE IF NOT EXISTS service " +
                        "(service_id BIGINT PRIMARY KEY," +
                        " name VARCHAR(1000)," +
                        " description CLOB," +
                        " image_id BIGINT," +
                        " duration INTEGER," +
                        " cost DECIMAL," +
                        " owner_id BIGINT)");

        exec("seq_repeat_id drop", "DROP SEQUENCE IF EXISTS seq_repeat_id");
        exec("repeat drop", "DROP TABLE IF EXISTS repeat");
        exec("seq_repeat_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_repeat_id");

        exec("repeat",
                "CREATE TABLE IF NOT EXISTS repeat " +
                        "(repeat_id BIGINT PRIMARY KEY, " +
                        " service_id BIGINT," +
                        " dow INTEGER," +
                        " duration INTEGER," +
                        " time_from INTEGER," +
                        " time_to INTEGER)");

        /*exec("schedule",
                "CREATE TABLE IF NOT EXISTS schedule " +
                        "(schedule_id BIGINT PRIMARY KEY, " +
                        " repeat_id BIGINT," +
                        " client_id BIGINT," +
                        " date_from DATE," +
                        " duration INTEGER," +
                        " title  VARCHAR2(4000)," +
                        " height INTEGER," +
                        " description CLOB," +
                        " bitmap BLOB," +
                        " owner_id BIGINT)");*/

    }

    /**
     * Test queries on local DB
     *
     * @param args
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {
        Updater.createStructures();
    }

}