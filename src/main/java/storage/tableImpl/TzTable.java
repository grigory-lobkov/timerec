package storage.tableImpl;

import model.TzRow;
import storage.IConnectionPool;
import storage.ITable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC storage access to {@code model.TzRow} objects
 */
public class TzTable implements ITable<TzRow> {

    private IConnectionPool pool;
    private String preSeqNextval;
    private String postSeqNextval;

    public TzTable(IConnectionPool connection) {
        pool = connection;
        preSeqNextval = pool.preSeqNextval();
        postSeqNextval = pool.postSeqNextval();
    }


    /**
     * Get {@code model.TzRow} object from storage by {@code tz_id}
     *
     * @param object_id object identifier
     * @return {@code model.TzRow} object
     * @throws Exception on error accessing storage
     */
    public TzRow select(long object_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM tz WHERE tz_id = ?");
        ps.setLong(1, object_id);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            TzRow r = new TzRow();
            r.tz_id = rs.getLong("tz_id");
            r.utc_offset = rs.getInt("utc_offset");
            r.name = rs.getString("name");
            return r;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Get {@code model.TzRow} object from storage by {@code utc_offset}
     *
     * @param filter object
     * @return {@code model.TzRow} object
     * @throws Exception on error accessing storage
     */
    public TzRow select(String filter) throws Exception {
        Integer utc = Integer.valueOf(filter);
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM tz WHERE utc_offset = ? ORDER BY tz_id");
        ps.setInt(1, utc);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            TzRow r = new TzRow();
            r.tz_id = rs.getLong("tz_id");
            r.utc_offset = rs.getInt("utc_offset");
            r.name = rs.getString("name");
            return r;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Set {@code model.TzRow} object to storage by {@code tz.tz_id}
     *
     * @param tz updated object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean update(TzRow tz) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE tz SET utc_offset = ?, name = ? WHERE tz_id = ?");
        try {
            ps.setInt(1, tz.utc_offset);
            ps.setString(2, tz.name);
            ps.setLong(3, tz.tz_id);

            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Create new {@code model.TzRow} object in storage
     * {@code tz.tz_id} will be updated to new value
     *
     * @param tz new object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean insert(TzRow tz) throws Exception {
        Connection conn = pool.connection();
        String resultColumns[] = new String[]{"tz_id"};
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO tz (tz_id, utc_offset, name)" +
                        "VALUES (" + preSeqNextval + "seq_tz_id" + postSeqNextval + ", ?, ?)", resultColumns);
        try {
            ps.setInt(1, tz.utc_offset);
            ps.setString(2, tz.name);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    tz.tz_id = generatedKeys.getLong(1);
                }
            }
            return affectedRows == 1;
        } finally {
            ps.close();
            conn.close();
        }
    }

    /**
     * Delete {@code model.TzRow} object from storage by {@code tz_id}
     *
     * @param object_id
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public boolean delete(long object_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM tz WHERE tz_id = ?");
        ps.setLong(1, object_id);
        try {
            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Reads all from list of tz
     *
     * @return list of {@code model.TzRow} objects
     * @throws Exception
     */
    @Override
    public List<TzRow> select() throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT tz_id, utc_offset, name FROM tz ORDER BY utc_offset");
        ResultSet rs = ps.executeQuery();
        List<TzRow> list = new ArrayList<TzRow>();
        try {
            while (rs.next()) {
                TzRow r = new TzRow();
                r.tz_id = rs.getLong("tz_id");
                r.utc_offset = rs.getInt("utc_offset");
                r.name = rs.getString("name");
                list.add(r);
            }
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
        return list;
    }


    /*
     * Check model.TzRow object {@code object_id} is owned by {@code user_id}
     *
     * @param object_id object to check
     * @param user_id user to check
     * @return true if {@code user_id} is owner of object {@code object_id}

    @Override
    public boolean checkIsOwner(long object_id, long user_id) throws SQLException {
        PreparedStatement ps = connectImpl.prepareStatement(
                "SELECT * FROM tz WHERE tz_id = ? AND owner_id = ?");
        ps.setLong(1, object_id);
        ps.setLong(2, user_id);
        ResultSet rs = ps.executeQuery();
        try {
            return rs.next();
        } finally {
            rs.close();
            ps.close();
        }
    }*/

}