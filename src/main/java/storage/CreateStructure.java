package storage;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CreateStructure implements ServletContextListener {

    private boolean debugLog = false;

    /**
     * Check, if we need to create table structure
     *
     * @return true, if need
     */
    boolean checkStructureNeedCreate() {
        boolean result = true;
        try (Connection conn = StorageFactory.dbPool.connection()) {
//            DatabaseMetaData meta = conn.getMetaData();
//            ResultSet res = meta.getTables("", null, "setting", new String[]{"TABLE"}); // case-dependent search in PostgreSQL, deprecated by me
//            while (res.next()) result = false;
//            res.close();
            Statement statement = conn.createStatement();
            try {
                result = !statement.execute("SELECT user_id FROM users"); // query, which will raise exception if create structure needed
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            System.out.println("checkStructureNeedCreate() " + e.getMessage());
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
        StorageFactory.dbPool.close();
    }

}

class Updater {

    // Default users
    static final String ADMIN_NAME = "Admin";
    static final String ADMIN_EMAIL = "admin@timerec.ru";
    static final String ADMIN_PASSWORD = "admin";
    static final String CLIENT_NAME = "Client";
    static final String CLIENT_EMAIL = "client@timerec.ru";
    static final String CLIENT_PASSWORD = "client";

    // Basic roles
    static final String ROLE_ADMIN = "Administrators";
    static final String ROLE_CLIENT = "Clients";
    static final String ROLE_PUBLIC = "Public";

    // Default settings
    static final String CLIENT_LIMIT_DAILY = "2";
    static final String CLIENT_LIMIT_WEEKLY = "4";
    static final String CLIENT_LIMIT_MONTHLY = "10";

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
        try {
            statement.executeUpdate(query);
            System.out.println("ok");
        } catch (Exception e) {
            System.out.println(query);
            throw e;
        }
    }

    static String fromDual;
    static String preSeqNextval;
    static String postSeqNextval;

    /**
     * Creates table structure and indexes
     *
     * @throws SQLException on storage problems
     */
    static void createStructures() throws SQLException {
        Connection conn = StorageFactory.dbPool.connection();
        fromDual = StorageFactory.dbPool.fromDual();
        preSeqNextval = StorageFactory.dbPool.preSeqNextval();
        postSeqNextval = StorageFactory.dbPool.postSeqNextval();

        statement = conn.createStatement();

        //exec("image_filename_idx drop", "DROP INDEX IF EXISTS image_filename_idx");
        //exec("image drop", "DROP TABLE IF EXISTS image");
        //exec("seq_image_id drop", "DROP SEQUENCE IF EXISTS seq_image_id");

        exec("seq_image_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_image_id");

        exec("image",
                "CREATE TABLE IF NOT EXISTS image " +
                        "(image_id BIGINT PRIMARY KEY, " +
                        " filename VARCHAR(100)," +
                        " altname VARCHAR(1000)," +
                        " width INTEGER," +
                        " height INTEGER," +
                        " bitmap TEXT)");

        exec("image_pk_idx",
                "CREATE INDEX IF NOT EXISTS image_pk_idx ON image (image_id)");

        //exec("image_filename_idx",
        //        "CREATE INDEX IF NOT EXISTS image_filename_idx ON image (filename)");

        //exec("tz_utc_offset_idx drop", "DROP INDEX IF EXISTS tz_utc_offset_idx");
        //exec("tz_pk_idx drop", "DROP INDEX IF EXISTS tz_pk_idx");
        //exec("tz drop", "DROP TABLE IF EXISTS tz");
        //exec("seq_tz_id drop", "DROP SEQUENCE IF EXISTS seq_tz_id");

        exec("seq_tz_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_tz_id");

        exec("tz",
                "CREATE TABLE IF NOT EXISTS tz " +
                        "(tz_id BIGINT PRIMARY KEY," +
                        " utc_offset INTEGER," +
                        " name VARCHAR(1000)," +
                        " owner_id BIGINT)");

        exec("tz_update_list",
                "INSERT INTO tz (tz_id, utc_offset, name)" +
                        "SELECT " + preSeqNextval + "seq_tz_id" + postSeqNextval + " tz_id, utc_offset, name FROM (" +
                        " SELECT 2*60 utc_offset, '(UTC+2) Kaliningrad' as name " + fromDual + " UNION ALL" +
                        " SELECT 3*60, '(UTC+3) Moscow' " + fromDual + " UNION ALL" +
                        " SELECT 4*60, '(UTC+4) Samara' " + fromDual + " UNION ALL" +
                        " SELECT 5*60, '(UTC+5) Yekaterinburg' " + fromDual + " UNION ALL" +
                        " SELECT 6*60, '(UTC+6) Omsk' " + fromDual + " UNION ALL" +
                        " SELECT 7*60, '(UTC+7) Krasnoyarsk, Novosibirsk' " + fromDual + " UNION ALL" +
                        " SELECT 8*60, '(UTC+8) Irkutsk' " + fromDual + " UNION ALL" +
                        " SELECT 9*60, '(UTC+9) Yakutsk, Chita' " + fromDual + " UNION ALL" +
                        " SELECT 10*60, '(UTC+10) Vladivostok' " + fromDual + " UNION ALL" +
                        " SELECT 11*60, '(UTC+11) Magadan, Sakhalinsk, Srednekolymsk' " + fromDual + " UNION ALL" +
                        " SELECT 12*60, '(UTC+12) Anadyr, Petropavlovsk-Kamchatsky' " + fromDual + "" +
                        ")x WHERE (name) NOT IN (SELECT name FROM tz)");

        exec("tz_pk_idx",
                "CREATE INDEX IF NOT EXISTS tz_pk_idx ON tz (tz_id)");

        exec("tz_utc_offset_idx",
                "CREATE INDEX IF NOT EXISTS tz_utc_offset_idx ON tz (utc_offset)");

        userRole();

        userAccess();

        user(); // must be after userRole()

        serviceSchedule();

        setting();

        statement.close();
        conn.close();
    }

    /**
     * SETTING table creation
     *
     * @throws SQLException
     */
    private static void setting() throws SQLException {
        exec("seq_setting_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_setting_id");

        exec("setting",
                "CREATE TABLE IF NOT EXISTS setting" +
                        "(setting_id BIGINT PRIMARY KEY," +
                        " alias VARCHAR(64)," +
                        " name VARCHAR(1000)," +
                        " description TEXT," +
                        " value VARCHAR(4000)," +
                        " service_id BIGINT," +
                        " owner_id BIGINT)");

        exec("setting_pk_idx",
                "CREATE INDEX IF NOT EXISTS setting_pk_idx ON setting (setting_id)");

//        no need indexes, cause of api.setting.SettingController stores it in memory
//        exec("setting_service_idx",
//                "CREATE INDEX IF NOT EXISTS setting_service_idx ON setting (service_id)");
//        exec("setting_alias_idx",
//                "CREATE INDEX IF NOT EXISTS setting_alias_idx ON setting (alias)");

        exec("setting_update_list",
                "INSERT INTO setting (setting_id, alias, name, description, value)" +
                        "SELECT " + preSeqNextval + "seq_setting_id" + postSeqNextval + " setting_id, alias, name, description, value FROM (" +
                        "  SELECT 'ALL_SERVICES_CLIENT_LIMIT_DAILY' as alias, '" + CLIENT_LIMIT_DAILY + "' as value," +
                        "         'All services usage limit per day' as name, 'Client cannot take more than this count of services per day.' as description " + fromDual + " UNION ALL" +
                        "  SELECT 'ALL_SERVICES_CLIENT_LIMIT_WEEKLY', '" + CLIENT_LIMIT_WEEKLY + "'," +
                        "         'All services usage limit per week', 'Client cannot take more than this count of services per week.' " + fromDual + " UNION ALL" +
                        "  SELECT 'ALL_SERVICES_CLIENT_LIMIT_MONTHLY', '" + CLIENT_LIMIT_MONTHLY + "'," +
                        "         'All services usage limit per month', 'Client cannot take more than this count of services per month.' " + fromDual + "" +
                        ")x WHERE (alias) NOT IN (SELECT alias FROM setting)");
    }

    /**
     * USER table creation
     *
     * @throws SQLException
     */
    private static void user() throws SQLException {

        //exec("seq_users_id drop", "DROP SEQUENCE IF EXISTS seq_users_id");
        //exec("user_email_idx drop", "DROP INDEX IF EXISTS user_email_idx");
        //exec("user drop", "DROP TABLE IF EXISTS user");

        exec("seq_users_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_users_id");

        exec("users",
                "CREATE TABLE IF NOT EXISTS users " +
                        "(user_id BIGINT PRIMARY KEY," +
                        " role_id BIGINT," +
                        " name VARCHAR(1000)," +
                        " tz_id BIGINT," +
                        " email VARCHAR(100)," +
                        " password VARCHAR(100)," +
                        " image_id BIGINT," +
                        " owner_id BIGINT)");

        exec("users_pk_idx",
                "CREATE INDEX IF NOT EXISTS users_pk_idx ON users (user_id)");

        exec("users_email_idx",
                "CREATE INDEX IF NOT EXISTS users_email_idx ON users (email)");


        exec("users_insert_admin",
                "INSERT INTO users (user_id, role_id, name, tz_id, email, password)" +
                        "SELECT " + preSeqNextval + "seq_users_id" + postSeqNextval + ", role_id, '" + ADMIN_NAME + "', 2, '" + ADMIN_EMAIL + "', '" + Passwords.encrypt(ADMIN_PASSWORD) + "'" +
                        "FROM role " +
                        "WHERE name = '" + ROLE_ADMIN + "' AND (SELECT COUNT(user_id) FROM users) = 0");

        exec("users_insert_client",
                "INSERT INTO users (user_id, role_id, name, tz_id, email, password)" +
                        "SELECT " + preSeqNextval + "seq_users_id" + postSeqNextval + ", role_id, '" + CLIENT_NAME + "', 2, '" + CLIENT_EMAIL + "', '" + Passwords.encrypt(CLIENT_PASSWORD) + "'" +
                        "FROM role " +
                        "WHERE name = '" + ROLE_CLIENT + "' AND (SELECT COUNT(user_id) FROM users) = 1");
    }

    /**
     * ROLE table creation
     *
     * @throws SQLException
     */
    private static void userRole() throws SQLException {

        exec("seq_role_id drop", "DROP SEQUENCE IF EXISTS seq_role_id");
        exec("role drop", "DROP TABLE IF EXISTS role");

        exec("seq_role_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_role_id");

        exec("role",
                "CREATE TABLE IF NOT EXISTS role " +
                        "(role_id BIGINT PRIMARY KEY," +
                        " name VARCHAR(1000)," +
                        " owner_id BIGINT," +
                        " is_default BOOLEAN)");

        exec("role_pk_idx",
                "CREATE INDEX IF NOT EXISTS role_pk_idx ON role (role_id)");

        exec("role_update_list_public", // PUBLIC USER ROLE MUST HAVE role_id=1 ALWAYS, SessiotUtils.getPublicUser() LIMITATION
                "INSERT INTO role (role_id, name, is_default)" +
                        "SELECT " + preSeqNextval + "seq_role_id" + postSeqNextval + " role_id, name, false FROM (" +
                        " SELECT '" + ROLE_PUBLIC + "' as name " + fromDual + "" +
                        ")x WHERE (name) NOT IN (SELECT name FROM role)");

        exec("role_update_list",
                "INSERT INTO role (role_id, name, is_default)" +
                        "SELECT " + preSeqNextval + "seq_role_id" + postSeqNextval + " role_id, name, def FROM (" +
                        " SELECT '" + ROLE_ADMIN + "' as name, false def " + fromDual + " UNION ALL" +
                        " SELECT '" + ROLE_CLIENT + "' as name, true def " + fromDual + "" +
                        ")x WHERE (name) NOT IN (SELECT name FROM role)");
    }

    /**
     * ACCESS table creation
     *
     * @throws SQLException
     */
    private static void userAccess() throws SQLException {

        exec("seq_access_id drop", "DROP SEQUENCE IF EXISTS seq_access_id");
        exec("access drop", "DROP TABLE IF EXISTS access");

        exec("seq_access_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_access_id");

        exec("access",
                "CREATE TABLE IF NOT EXISTS access " +
                        "(access_id BIGINT PRIMARY KEY," +
                        " role_id BIGINT," +
                        " object_name VARCHAR(100)," +
                        " all_get BOOLEAN," +
                        " all_put BOOLEAN," +
                        " all_post BOOLEAN," +
                        " all_delete BOOLEAN," +
                        " own_get BOOLEAN," +
                        " own_put BOOLEAN," +
                        " own_post BOOLEAN," +
                        " own_delete BOOLEAN)");

        exec("access_pk_idx",
                "CREATE INDEX IF NOT EXISTS access_pk_idx ON access (access_id)");

        exec("access_insert_role_admin",
                "INSERT INTO access (access_id, role_id, object_name," +
                        " all_get, all_put, all_post, all_delete, own_get, own_put, own_post, own_delete)" +
                        "SELECT " + preSeqNextval + "seq_access_id" + postSeqNextval + ", role_id, page.name, true, true, true, true, true, true, true, true " +
                        "FROM role, (" +
                        " SELECT 'menu' as name " + fromDual + " UNION ALL" +
                        " SELECT 'setting' " + fromDual + " UNION ALL" +
                        " SELECT 'user' " + fromDual + " UNION ALL" +
                        " SELECT 'profile' " + fromDual + " UNION ALL" +
                        " SELECT 'role' " + fromDual + " UNION ALL" +
                        " SELECT 'access' " + fromDual + " UNION ALL" +
                        " SELECT 'image' " + fromDual + " UNION ALL" +
                        " SELECT 'service' " + fromDual + " UNION ALL" +
                        " SELECT 'repeat' " + fromDual + " UNION ALL" +
                        " SELECT 'schedule' " + fromDual + " UNION ALL" +
                        " SELECT 'tz' " + fromDual + " UNION ALL" +
                        " SELECT 'login' " + fromDual + " UNION ALL" +
                        " SELECT 'logout' " + fromDual + "" +
                        ") page " +
                        "WHERE role.name = '" + ROLE_ADMIN + "' AND NOT EXISTS (SELECT 1 FROM access WHERE role_id=role.role_id)");

        exec("access_insert_role_client",
                "INSERT INTO access (access_id, role_id, object_name," +
                        " all_get, all_put, all_post, all_delete, own_get, own_put, own_post, own_delete)" +
                        "SELECT " + preSeqNextval + "seq_access_id" + postSeqNextval + ", role_id, page.name, true, true, true, true, true, true, true, true " +
                        "FROM role, (" +
                        " SELECT 'menu' as name " + fromDual + " UNION ALL" +
                        " SELECT 'profile' " + fromDual + " UNION ALL" +
                        " SELECT 'record' " + fromDual + " UNION ALL" +
                        " SELECT 'records' " + fromDual + " UNION ALL" +
                        " SELECT 'login' " + fromDual + " UNION ALL" +
                        " SELECT 'logout' " + fromDual + "" +
                        ") page " +
                        "WHERE role.name = '" + ROLE_CLIENT + "' AND NOT EXISTS (SELECT 1 FROM access WHERE role_id=role.role_id)");

        exec("access_insert_role_public",
                "INSERT INTO access (access_id, role_id, object_name," +
                        " all_get, all_put, all_post, all_delete, own_get, own_put, own_post, own_delete)" +
                        "SELECT " + preSeqNextval + "seq_access_id" + postSeqNextval + ", role_id, page.name, true, true, true, true, true, true, true, true " +
                        "FROM role, (" +
                        " SELECT 'login' as name " + fromDual + " UNION ALL" +
                        " SELECT 'register' " + fromDual + "" +
                        ") page " +
                        "WHERE role.name = '" + ROLE_PUBLIC + "' AND NOT EXISTS (SELECT 1 FROM access WHERE role_id=role.role_id)");
    }

    /**
     * SERVICE, REPEAT, SCHEDULE tables creation
     *
     * @throws SQLException
     */
    private static void serviceSchedule() throws SQLException {

        //exec("seq_service_id drop", "DROP SEQUENCE IF EXISTS seq_service_id");
        //exec("service drop", "DROP TABLE IF EXISTS service");

        exec("seq_service_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_service_id");

        exec("service",
                "CREATE TABLE IF NOT EXISTS service " +
                        "(service_id BIGINT PRIMARY KEY," +
                        " name VARCHAR(1000)," +
                        " description TEXT," +
                        " image_id BIGINT," +
                        " duration INTEGER," +
                        " cost DECIMAL," +
                        " owner_id BIGINT)");

        exec("service_pk_idx",
                "CREATE INDEX IF NOT EXISTS service_pk_idx ON service (service_id)");

        //exec("seq_repeat_id drop", "DROP SEQUENCE IF EXISTS seq_repeat_id");
        //exec("repeat_service_idx drop", "DROP INDEX IF EXISTS repeat_service_idx");
        //exec("repeat drop", "DROP TABLE IF EXISTS repeat");

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

        exec("repeat_pk_idx",
                "CREATE INDEX IF NOT EXISTS repeat_pk_idx ON repeat (repeat_id)");

        exec("repeat_service_idx",
                "CREATE INDEX IF NOT EXISTS repeat_service_idx ON repeat (service_id)");

        //exec("schedule drop", "DROP TABLE IF EXISTS schedule");

        exec("seq_schedule_id",
                "CREATE SEQUENCE IF NOT EXISTS seq_schedule_id");

        exec("schedule",
                "CREATE TABLE IF NOT EXISTS schedule " +
                        "(schedule_id BIGINT PRIMARY KEY," +
                        " service_id BIGINT," +
                        " user_id BIGINT," +
                        " date_from TIMESTAMP WITHOUT TIME ZONE," +
                        " duration INTEGER," +
                        " title VARCHAR(4000)," +
                        " description TEXT)");

        exec("schedule_pk_idx",
                "CREATE INDEX IF NOT EXISTS schedule_pk_idx ON schedule (schedule_id)");

        exec("schedule_service_date_idx",
                "CREATE INDEX IF NOT EXISTS schedule_service_date_idx ON schedule (service_id, date_from)");

        exec("schedule_user_date_idx",
                "CREATE INDEX IF NOT EXISTS schedule_user_date_idx ON schedule (user_id, date_from)");

        exec("schedule_date_idx",
                "CREATE INDEX IF NOT EXISTS schedule_date_idx ON schedule (date_from)");

    }

    /**
     * Test queries on local DB
     *
     * @param args
     * @throws SQLException
     */
    public static void main(String[] args) throws SQLException {
        Updater.createStructures();
        //int a = 123451234512345;
        List list = new ArrayList();

    }

}