package storage.jdbc;

import storage.StorageFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.sql.*;

/**
 * JDBC structure creating
 *
 * Servlet must be in web.xml
 * <web-app>
 *   ...
 *   <servlet><!-- prepare storage to use, destroy connection on close-->
 *     <servlet-name>initializer</servlet-name>
 *     <servlet-class>storage.jdbc.CreateStructure</servlet-class>
 *     <load-on-startup>1</load-on-startup>
 *   </servlet>
 * </web-app>
 */
public class CreateStructure extends HttpServlet {

    private boolean debugLog = false;

    /**
     * Check, if we need to create table structure
     *
     * @return true, if need
     */
    boolean checkStructureNeedCreate() {
        boolean result = true;
        try {
            DatabaseMetaData meta = StorageFactory.connection.getMetaData();
            ResultSet res = meta.getTables("", null, "SERVICE", new String[]{"TABLE"});
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
     * @param config not used
     * @throws ServletException on storage problems
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        if (debugLog) System.out.println("CreateStructure.init()");
        try {
            System.out.println("Check database...");
            if (checkStructureNeedCreate()) {
                System.out.println("Create structures...");
                createStructures();
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
     * One statement for all scripts, executed here
     */
    Statement statement;

    /**
     * Help method to beautify output and logging
     *
     * @param action log message
     * @param query string to execute by JDBC
     * @throws SQLException on storage problems
     */
    void exec(String action, String query) throws SQLException {
        System.out.print(action + " ... ");
        statement.executeUpdate(query);
        System.out.println("ok");
    }

    /**
     * Creates table structure and indexes
     *
     * @throws SQLException on storage problems
     */
    void createStructures() throws SQLException {
        statement = StorageFactory.connection.createStatement();

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

        statement.close();
    }


    /**
     * Destruct servlet: at the most end
     *
     * Needed to close connection.
     * If not - Tomcat will not update WebServlet automatically.
     */
    @Override
    public void destroy() {
        StorageFactory.dbConn.close();
    }
}