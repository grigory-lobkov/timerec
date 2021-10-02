package storage.tableImpl;

import model.AccessRow;
import storage.IConnectionPool;
import storage.ITable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC storage access to {@code model.AccessRow} objects
 */
public class AccessTable implements ITable<AccessRow> {

    private final IConnectionPool pool;
    private final String preSeqNextval;
    private final String postSeqNextval;
    private final String preSeqCurrval;
    private final String postSeqCurrval;
    private final String fromDual;

    public AccessTable(IConnectionPool connection) {
        pool = connection;
        preSeqNextval = pool.preSeqNextval();
        postSeqNextval = pool.postSeqNextval();
        preSeqCurrval = pool.preSeqCurrval();
        postSeqCurrval = pool.postSeqCurrval();
        fromDual = pool.fromDual();
    }

    /**
     * Get {@code model.AccessRow} object from storage by {@code access_id}
     *
     * @param objectId object identifier
     * @return {@code model.AccessRow} object
     * @throws Exception on error accessing storage
     */
    public AccessRow select(long objectId) throws Exception {
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM access WHERE access_id = ?")
        ) {
            ps.setLong(1, objectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return extractRow(rs);
            }
        }
    }

    /**
     * Get {@code model.AccessRow} object from storage by {@code email}
     *
     * @param filter object
     * @return {@code model.AccessRow} object
     * @throws Exception on error accessing storage
     */
    public AccessRow select(String filter) throws Exception {
        return null;
    }

    /**
     * Set {@code model.AccessRow} object to storage by {@code access.access_id}
     *
     * @param access updated object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean update(AccessRow access) throws Exception {
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE access SET role_id = ?, object_name = ?," +
                             " all_get = ?, all_put = ?, all_post = ?, all_delete = ?," +
                             " own_get = ?, own_put = ?, own_post = ?, own_delete = ?" +
                             " WHERE access_id = ?")
        ) {
            setFields(ps, access);
            ps.setLong(11, access.access_id);

            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        }
    }

    private void setFields(PreparedStatement ps, AccessRow access) throws SQLException {
        ps.setLong(1, access.role_id);
        ps.setString(2, access.object_name);
        ps.setBoolean(3, access.all_get);
        ps.setBoolean(4, access.all_put);
        ps.setBoolean(5, access.all_post);
        ps.setBoolean(6, access.all_delete);
        ps.setBoolean(7, access.own_get);
        ps.setBoolean(8, access.own_put);
        ps.setBoolean(9, access.own_post);
        ps.setBoolean(10, access.own_delete);
    }

    /**
     * Create new {@code model.AccessRow} object in storage
     * {@code access.access_id} will be updated to new value
     *
     * @param access new object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean insert(AccessRow access) throws Exception {
        String[] resultColumns = new String[]{"access_id"};
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO access (access_id, role_id, object_name," +
                             " all_get, all_put, all_post, all_delete," +
                             " own_get, own_put, own_post, own_delete)" +
                             "VALUES (" + preSeqNextval + "seq_access_id" + postSeqNextval + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", resultColumns)
        ) {
            setFields(ps, access);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    access.access_id = generatedKeys.getLong(1);
                } else { // when getGeneratedKeys() is not supported by DBMS
                    try (PreparedStatement csPs = conn.prepareStatement(
                            "SELECT " + preSeqCurrval + "seq_access_id" + postSeqCurrval + " " + fromDual)
                    ) {
                        ResultSet csRs = csPs.executeQuery();
                        if (csRs.next()) {
                            access.access_id = csRs.getLong(1);
                        }
                    }
                }
            }
            return affectedRows == 1;
        }
    }

    /**
     * Delete {@code model.AccessRow} object from storage by {@code access_id}
     *
     * @param objectId id
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean delete(long objectId) throws Exception {
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM access WHERE access_id = ?")
        ) {
            ps.setLong(1, objectId);
            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        }
    }

    /**
     * Reads all table
     *
     * @return list of {@code model.AccessRow} objects
     * @throws Exception db access
     */
    public List<AccessRow> select() throws Exception {
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM access")
        ) {
            ResultSet rs = ps.executeQuery();
            List<AccessRow> list = new ArrayList<>();
            while (rs.next()) {
                list.add(extractRow(rs));
            }
            return list;
        }
    }

    private AccessRow extractRow(ResultSet rs) throws SQLException {
        AccessRow r = new AccessRow();
        r.access_id = rs.getLong("access_id");
        r.role_id = rs.getLong("role_id");
        r.object_name = rs.getString("object_name");
        r.all_get = rs.getBoolean("all_get");
        r.all_put = rs.getBoolean("all_put");
        r.all_post = rs.getBoolean("all_post");
        r.all_delete = rs.getBoolean("all_delete");
        r.own_get = rs.getBoolean("own_get");
        r.own_put = rs.getBoolean("own_put");
        r.own_post = rs.getBoolean("own_post");
        r.own_delete = rs.getBoolean("own_delete");
        return r;
    }

}
