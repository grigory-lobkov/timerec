package storage;

import java.sql.*;

/**
 * Interface for class, returning one JDBC connection
 */
public interface IConnect {

    /**
     * Connection to JDBC
     */
    Connection connection();

    /**
     * Close connection
     *
     * If not - Tomcat will not update WebServlet automatically.
     */
    void close();

}