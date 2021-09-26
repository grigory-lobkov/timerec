package storage.tableImpl;

import model.RepeatRow;
import storage.IConnectionPool;
import storage.IMultiRowTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC storage access to {@code model.RepeatRow} objects
 *
 * Parent filter field = "service_id"
 */
public class RepeatTable implements IMultiRowTable<RepeatRow> {

    private IConnectionPool pool;
    private String preSeqNextval;
    private String postSeqNextval;


    public RepeatTable(IConnectionPool connection) {
        pool = connection;
        preSeqNextval = pool.preSeqNextval();
        postSeqNextval = pool.postSeqNextval();
    }


    /**
     * Get {@code model.RepeatRow} object from storage by {@code repeat_id}
     *
     * @param ids object identifiers
     * @return list of {@code model.RepeatRow} objects
     * @throws Exception on error accessing storage
     */
    @Override
    public List<RepeatRow> select(List<Long> ids) throws Exception {
        if (ids.size() == 0)
            return new ArrayList<>();

        StringBuilder commaList = new StringBuilder(ids.size() * 7);
        for (Long id : ids) {
            commaList.append((commaList.length() == 0 ? "" : ",") + id);
        }

        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM repeats WHERE repeat_id IN (" + commaList + ")");
        ResultSet rs = ps.executeQuery();

        List<RepeatRow> result = new ArrayList<>();
        try {
            while (rs.next()) {
                RepeatRow r = new RepeatRow();
                r.repeat_id = rs.getLong("repeat_id");
                r.service_id = rs.getLong("service_id");
                r.dow = rs.getInt("dow");
                r.duration = rs.getInt("duration");
                r.time_from = rs.getInt("time_from");
                r.time_to = rs.getInt("time_to");
                result.add(r);
            }
            return result;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Get {@code model.RepeatRow} object from storage by {@code repeat_id}
     *
     * @param parent_id object identifier
     * @return {@code model.RepeatRow} object
     * @throws Exception on error accessing storage
     */
    @Override
    public List<RepeatRow> select(long parent_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM repeats WHERE service_id = ?");
        ps.setLong(1, parent_id);
        ResultSet rs = ps.executeQuery();

        List<RepeatRow> result = new ArrayList<RepeatRow>();
        try {
            while (rs.next()) {
                RepeatRow r = new RepeatRow();
                r.repeat_id = rs.getLong("repeat_id");
                r.service_id = rs.getLong("service_id");
                r.dow = rs.getInt("dow");
                r.duration = rs.getInt("duration");
                r.time_from = rs.getInt("time_from");
                r.time_to = rs.getInt("time_to");
                result.add(r);
            }
            return result;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Set {@code model.RepeatRow} object to storage by {@code repeat.repeat_id}
     *
     * @param objects updated objects
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public int update(List<RepeatRow> objects) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE repeats SET service_id = ?, dow = ?, duration = ?, time_from = ?, time_to = ?" +
                        " WHERE repeat_id = ?");

        int affectedRows = 0;
        try {
            for (RepeatRow object : objects) {
                ps.setLong(1, object.service_id);
                ps.setInt(2, object.dow);
                ps.setInt(3, object.duration);
                ps.setInt(4, object.time_from);
                ps.setInt(5, object.time_to);
                ps.setLong(6, object.repeat_id);

                affectedRows += ps.executeUpdate();
            }
            return affectedRows;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Create new {@code model.RepeatRow} object in storage
     * {@code repeat.repeat_id} will be updated to new value
     *
     * @param objects new objects list
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public int insert(List<RepeatRow> objects) throws Exception {
        Connection conn = pool.connection();
        String resultColumns[] = new String[]{"repeat_id"};
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO repeats (repeat_id, service_id, dow, duration, time_from, time_to)" +
                        "VALUES (" + preSeqNextval + "seq_repeat_id" + postSeqNextval + ", ?, ?, ?, ?, ?)", resultColumns);

        int affectedRows = 0;
        try {
            for (RepeatRow object : objects) {
                ps.setLong(1, object.service_id);
                ps.setInt(2, object.dow);
                ps.setInt(3, object.duration);
                ps.setInt(4, object.time_from);
                ps.setInt(5, object.time_to);

                if (ps.executeUpdate() == 1) {
                    affectedRows++;
                    ResultSet generatedKeys = ps.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        object.repeat_id = generatedKeys.getLong(1);
                    }
                }
            }
            return affectedRows;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Delete {@code model.RepeatRow} object from storage by {@code parent_id}
     *
     * @param parent_id filter field
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public int delete(long parent_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM repeats WHERE service_id = ?");
        ps.setLong(1, parent_id);
        try {
            int affectedRows = ps.executeUpdate();
            return affectedRows;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Delete {@code model.RepeatRow} object from storage by {@code ids}
     *
     * @param ids object identifiers
     * @return true on success
     * @throws Exception on error accessing storage
     */
    @Override
    public int delete(List<Long> ids) throws Exception {
        if (ids.size() == 0)
            return 0;

        StringBuilder commaList = new StringBuilder(ids.size() * 7);
        for (Long id : ids) {
            commaList.append((commaList.length() == 0 ? "" : ",") + id);
        }
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM repeats WHERE repeat_id IN (" + commaList + ")");
        try {
            int affectedRows = ps.executeUpdate();
            return affectedRows;
        } finally {
            ps.close();
            conn.close();
        }
    }

}
