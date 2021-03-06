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

    /**
     * Post-statement to get data, not stored in any table
     *
     * @return
     */
    String fromDual();

    /**
     * Text before sequence name to extract next value
     *
     * @return
     */
    String preSeqNextval();

    /**
     * Text after sequence name to extract next value
     * @return
     */
    String postSeqNextval();

    /**
     * Text before string constants (Postgres to text conversion in subqueries)
     * @return
     */
    String preText();

    /**
     * Text after string constants (Postgres to text conversion in subqueries)
     * @return
     */
    String postText();

}