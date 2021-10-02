package storage.tableImpl;

import model.RoleRow;
import storage.IConnectionPool;
import storage.ITable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC storage access to {@code model.RoleRow} objects
 */
public class RoleTable implements ITable<RoleRow> {

    private final IConnectionPool pool;
    private final String preSeqNextval;
    private final String postSeqNextval;
    private final String preSeqCurrval;
    private final String postSeqCurrval;
    private final String fromDual;


    public RoleTable(IConnectionPool connection) {
        pool = connection;
        preSeqNextval = pool.preSeqNextval();
        postSeqNextval = pool.postSeqNextval();
        preSeqCurrval = pool.preSeqCurrval();
        postSeqCurrval = pool.postSeqCurrval();
        fromDual = pool.fromDual();
    }


    /**
     * Get {@code model.RoleRow} object from storage by {@code role_id}
     *
     * @param objectId object identifier
     * @return {@code model.RoleRow} object
     * @throws Exception on error accessing storage
     */
    public RoleRow select(long objectId) throws Exception {
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM role WHERE role_id = ?")
        ) {
            ps.setLong(1, objectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return extractRow(rs);
            }
        }
    }


    /**
     * Get {@code model.RoleRow} object from storage by smart filter
     *
     * @param filter object
     * @return {@code model.RoleRow} object
     * @throws Exception on error accessing storage
     */
    public RoleRow select(String filter) throws Exception {
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM role" +
                             " WHERE CASE" +
                             "  WHEN is_default THEN \"1\"" +
                             "  ELSE \"0\" END = ?")
        ) {
            ps.setString(1, filter);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return extractRow(rs);
            }
        }
    }


    /**
     * Set {@code model.RoleRow} object to storage by {@code role.role_id}
     *
     * @param role updated object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean update(RoleRow role) throws Exception {
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE role SET name = ?, is_default = ?" +
                             " WHERE role_id = ?")
        ) {
            setFields(ps, role);
            ps.setLong(3, role.role_id);
            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        }
    }

    private void setFields(PreparedStatement ps, RoleRow role) throws SQLException {
        ps.setString(1, role.name);
        ps.setBoolean(2, role.is_default);
    }


    /**
     * Create new {@code model.RoleRow} object in storage
     * {@code role.role_id} will be updated to new value
     *
     * @param role new object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean insert(RoleRow role) throws Exception {
        String[] resultColumns = new String[]{"role_id"};
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO role (role_id, name, is_default)" +
                             "VALUES (" + preSeqNextval + "seq_role_id" + postSeqNextval + ", ?, ?)", resultColumns)
        ) {
            setFields(ps, role);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    role.role_id = generatedKeys.getLong(1);
                } else { // when getGeneratedKeys() is not supported by DBMS
                    try (PreparedStatement csPs = conn.prepareStatement(
                            "SELECT " + preSeqCurrval + "seq_role_id" + postSeqCurrval + " " + fromDual)
                    ) {
                        ResultSet csRs = csPs.executeQuery();
                        if (csRs.next()) {
                            role.role_id = csRs.getLong(1);
                        }
                    }
                }
            }
            return affectedRows == 1;
        }
    }

    /**
     * Delete {@code model.RoleRow} object from storage by {@code role_id}
     *
     * @param objectId id
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean delete(long objectId) throws Exception {
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM role WHERE role_id = ?")
        ) {
            ps.setLong(1, objectId);
            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        }
    }


    /**
     * Reads list of all roles
     *
     * @return list of shortened {@code model.RoleRow} objects
     * @throws Exception db access
     */
    public List<RoleRow> select() throws Exception {
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM role ORDER BY name");
             ResultSet rs = ps.executeQuery()
        ) {
            List<RoleRow> list = new ArrayList<>();
            while (rs.next()) {
                list.add(extractRow(rs));
            }
            return list;
        }
    }

    private RoleRow extractRow(ResultSet rs) throws SQLException {
        RoleRow r = new RoleRow();
        r.role_id = rs.getLong("role_id");
        r.name = rs.getString("name");
        r.is_default = rs.getBoolean("is_default");
        return r;
    }


    /*
     * Check model.RoleRow object {@code object_id} is owned by {@code role_id}
     *
     * @param object_id object to check
     * @param role_id role to check
     * @return true if {@code role_id} is owner of object {@code object_id}

    public boolean checkIsOwner(long object_id, long role_id) throws SQLException {
        PreparedStatement ps = connectImpl.prepareStatement(
                "SELECT * FROM role WHERE role_id = ? AND owner_id = ?");
        ps.setLong(1, object_id);
        ps.setLong(2, role_id);
        ResultSet rs = ps.executeQuery();
        try {
            return rs.next();
        } finally {
            rs.close();
            ps.close();
        }
    }*/

}
