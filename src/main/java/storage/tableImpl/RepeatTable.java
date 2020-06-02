package storage.tableImpl;

import model.RepeatRow;
import storage.IConnect;
import storage.ITable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC storage access to {@code model.RepeatRow} objects
 */
public class RepeatTable implements ITable<RepeatRow> {

    /**
     * Connection fast access variable
     */
    IConnect dbConn;


    public RepeatTable(IConnect connection) {
        dbConn = connection;
    }


    /**
     * Get {@code model.RepeatRow} object from storage by {@code repeat_id}
     *
     * @param object_id object identifier
     * @return {@code model.RepeatRow} object
     * @throws Exception on error accessing storage
     */
    public RepeatRow select(long object_id) throws Exception {
        PreparedStatement ps = dbConn.connection().prepareStatement(
                "SELECT * FROM repeat WHERE repeat_id = ?");
        ps.setLong(1, object_id);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            RepeatRow r = new RepeatRow();
            r.repeat_id = rs.getLong("repeat_id");
            r.service_id = rs.getLong("service_id");
            r.dow = rs.getInt("dow");
            r.duration = rs.getInt("duration");
            r.timeFrom = rs.getDate("timeFrom");
            r.timeTo = rs.getDate("timeTo");
            return r;
        } finally {
            rs.close();
            ps.close();
        }
    }


    /**
     * Get {@code model.RepeatRow} object from storage by {@code filter}
     *
     * @param filter
     * @return {@code model.RepeatRow} object
     * @throws Exception on error accessing storage
     */
    public RepeatRow select(String filter) throws Exception {
        throw new RuntimeException("RepeatTable.select(String filter) not implemented.");
    }


    /**
     * Set {@code model.RepeatRow} object to storage by {@code repeat.repeat_id}
     *
     * @param repeat updated object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean update(RepeatRow repeat) throws Exception {
        PreparedStatement ps = dbConn.connection().prepareStatement(
                "UPDATE repeat SET service_id = ?, dow = ?, duration = ?, timeFrom = ?, timeTo = ?" +
                        " WHERE repeat_id = ?");
        try {
            ps.setLong(1, repeat.service_id);
            ps.setInt(2, repeat.dow);
            ps.setInt(3, repeat.duration);
            ps.setDate(4, repeat.timeFrom);
            ps.setDate(5, repeat.timeTo);
            ps.setLong(6, repeat.repeat_id);

            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        } finally {
            ps.close();
        }
    }


    /**
     * Create new {@code model.RepeatRow} object in storage
     * {@code repeat.repeat_id} will be update to new value
     *
     * @param repeat new object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean insert(RepeatRow repeat) throws Exception {
        String resultColumns[] = new String[]{"repeat_id"};
        PreparedStatement ps = dbConn.connection().prepareStatement(
                "INSERT INTO repeat (repeat_id, service_id, dow, duration, timeFrom, timeTo)" +
                        "VALUES (seq_repeat_id.nextval, ?, ?, ?, ?, ?)", resultColumns);
        try {
            ps.setLong(1, repeat.service_id);
            ps.setInt(2, repeat.dow);
            ps.setInt(3, repeat.duration);
            ps.setDate(4, repeat.timeFrom);
            ps.setDate(5, repeat.timeTo);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    repeat.repeat_id = generatedKeys.getLong(1);
                }
            }
            return affectedRows == 1;
        } finally {
            ps.close();
        }
    }


    /**
     * Delete {@code model.RepeatRow} object from storage by {@code repeat_id}
     *
     * @param object_id
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public boolean delete(long object_id) throws Exception {
        PreparedStatement ps = dbConn.connection().prepareStatement(
                "DELETE FROM repeat WHERE repeat_id = ?");
        ps.setLong(1, object_id);
        try {
            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        } finally {
            ps.close();
        }
    }


    /**
     * Reads only {@code repeat_id} and {@code name} from list of repeats
     *
     * @return list of shortened {@code model.RepeatRow} objects
     * @throws Exception
     */
    @Override
    public List<RepeatRow> selectAllQuick() throws Exception {
        throw new RuntimeException("RepeatTable.selectAllQuick() not implemented.");
    }

}