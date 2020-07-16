package storage;

import java.sql.Connection;

/**
 * Interface for class, returning one JDBC connection
 */
public interface IConnectionPool {

    /**
     * Get one JDBC connection
     */
    Connection connection();

    /**
     * Close pool
     *
     * If not - Tomcat will not update WebServlet automatically.
     */
    void close();

    String fromDual();

    String preSeqNextval();

    String postSeqNextval();

}