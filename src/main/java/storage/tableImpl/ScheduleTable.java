package storage.tableImpl;

import model.ScheduleRow;
import storage.IConnectionPool;
import storage.IScheduleTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC storage access to {@code model.ScheduleRow} objects
 */
public class ScheduleTable implements IScheduleTable<ScheduleRow> {
    
    /**
     * Connection fast access variable
     */
    IConnectionPool pool;

    public ScheduleTable(IConnectionPool connection) {
        pool = connection;
    }


    /**
     * Get {@code model.ScheduleRow} object from storage by {@code schedule_id}
     *
     * @param object_id object identifier
     * @return {@code model.ScheduleRow} object
     * @throws Exception on error accessing storage
     */
    public ScheduleRow select(long object_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT s.*, u.name user_name, e.name service_name" +
                        " FROM schedule s, user u, service e" +
                        " WHERE u.user_id = s.user_id AND e.service_id = s.service_id" +
                        "   AND s.schedule_id = ?");
        ps.setLong(1, object_id);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            ScheduleRow r = new ScheduleRow();
            r.schedule_id = rs.getLong("schedule_id");
            r.service_id = rs.getLong("service_id");
            r.user_id = rs.getLong("user_id");
            r.date_from = rs.getTimestamp("date_from");
            r.duration = rs.getInt("duration");
            r.title = rs.getString("title");
            r.description = rs.getString("description");
            r.user_name = rs.getString("user_name");
            return r;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Set {@code model.ScheduleRow} object to storage by {@code schedule.schedule_id}
     *
     * @param schedule updated object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean update(ScheduleRow schedule) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE schedule SET service_id = ?, user_id = ?, date_from = ?, duration = ?, title = ?, description = ?" +
                        " WHERE schedule_id = ?");
        try {
            ps.setLong(1, schedule.service_id);
            ps.setLong(2, schedule.user_id);
            ps.setTimestamp(3, schedule.date_from);
            ps.setInt(4, schedule.duration);
            ps.setString(5, schedule.title);
            ps.setString(6, schedule.description);
            ps.setLong(7, schedule.schedule_id);

            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Create new {@code model.ScheduleRow} object in storage
     * {@code schedule.schedule_id} will be update to new value
     *
     * @param schedule new object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean insert(ScheduleRow schedule) throws Exception {
        Connection conn = pool.connection();
        String resultColumns[] = new String[]{"schedule_id"};
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO schedule (schedule_id, service_id, user_id, date_from, duration, title, description)" +
                        "VALUES (seq_schedule_id.nextval, ?, ?, ?, ?, ?, ?)", resultColumns);
        try {
            ps.setLong(1, schedule.service_id);
            ps.setLong(2, schedule.user_id);
            ps.setTimestamp(3, schedule.date_from);
            ps.setInt(4, schedule.duration);
            ps.setString(5, schedule.title);
            ps.setString(6, schedule.description);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    schedule.schedule_id = generatedKeys.getLong(1);
                }
            }
            return affectedRows == 1;
        } finally {
            ps.close();
            conn.close();
        }
    }

    /**
     * Delete {@code model.ScheduleRow} object from storage by {@code schedule_id}
     *
     * @param object_id
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public boolean delete(long object_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM schedule WHERE schedule_id = ?");
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
     * Gets list of schedule by service_id
     *
     * @return list of {@code model.ScheduleRow} objects
     * @throws Exception
     */
    @Override
    public List<ScheduleRow> selectByService(long service_id, Timestamp date_from, Timestamp date_to) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT s.*, u.name user_name, e.name service_name" +
                        " FROM schedule s, user u, service e" +
                        " WHERE u.user_id = s.user_id AND e.service_id = s.service_id" +
                        "   AND s.service_id = ? AND s.date_from BETWEEN ? AND ?" +
                        " ORDER BY s.date_from");
        ps.setLong(1, service_id);
        ps.setTimestamp(2, date_from);
        ps.setTimestamp(3, date_to);

        ResultSet rs = ps.executeQuery();
        List<ScheduleRow> list = new ArrayList<ScheduleRow>();
        try {
            while (rs.next()) {
                ScheduleRow r = new ScheduleRow();
                r.schedule_id = rs.getLong("schedule_id");
                r.service_id = rs.getLong("service_id");
                r.user_id = rs.getLong("user_id");
                r.date_from = rs.getTimestamp("date_from");
                r.duration = rs.getInt("duration");
                r.title = rs.getString("title");
                r.description = rs.getString("description");
                r.user_name = rs.getString("user_name");
                r.service_name = rs.getString("service_name");
                list.add(r);
            }
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
        return list;
    }


    /**
     * Gets list of schedule by user_id
     *
     * @return list of {@code model.ScheduleRow} objects
     * @throws Exception
     */
    @Override
    public List<ScheduleRow> selectByUser(long user_id, Timestamp date_from, Timestamp date_to) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT s.*, u.name user_name, e.name service_name" +
                        " FROM schedule s, user u, service e" +
                        " WHERE u.user_id = s.user_id AND e.service_id = s.service_id" +
                        "   AND s.user_id = ? AND s.date_from BETWEEN ? AND ?" +
                        " ORDER BY s.date_from");
        ps.setLong(1, user_id);
        ps.setTimestamp(2, date_from);
        ps.setTimestamp(3, date_to);

        ResultSet rs = ps.executeQuery();
        List<ScheduleRow> list = new ArrayList<ScheduleRow>();
        try {
            while (rs.next()) {
                ScheduleRow r = new ScheduleRow();
                r.schedule_id = rs.getLong("schedule_id");
                r.service_id = rs.getLong("service_id");
                r.user_id = rs.getLong("user_id");
                r.date_from = rs.getTimestamp("date_from");
                r.duration = rs.getInt("duration");
                r.title = rs.getString("title");
                r.description = rs.getString("description");
                r.user_name = rs.getString("user_name");
                r.service_name = rs.getString("service_name");
                list.add(r);
            }
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
        return list;
    }


    /**
     * Gets list of schedule by date
     *
     * @return list of {@code model.ScheduleRow} objects
     * @throws Exception
     */
    @Override
    public List<ScheduleRow> selectByDate(Timestamp date_from, Timestamp date_to) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT s.*, u.name user_name, e.name service_name" +
                        " FROM schedule s, user u, service e" +
                        " WHERE u.user_id = s.user_id AND e.service_id = s.service_id" +
                        "   AND s.date_from BETWEEN ? AND ?" +
                        " ORDER BY s.date_from");
        ps.setTimestamp(1, date_from);
        ps.setTimestamp(2, date_to);

        ResultSet rs = ps.executeQuery();
        List<ScheduleRow> list = new ArrayList<ScheduleRow>();
        try {
            while (rs.next()) {
                ScheduleRow r = new ScheduleRow();
                r.schedule_id = rs.getLong("schedule_id");
                r.service_id = rs.getLong("service_id");
                r.user_id = rs.getLong("user_id");
                r.date_from = rs.getTimestamp("date_from");
                r.duration = rs.getInt("duration");
                r.title = rs.getString("title");
                r.description = rs.getString("description");
                r.user_name = rs.getString("user_name");
                r.service_name = rs.getString("service_name");
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
     * Check model.ScheduleRow object {@code object_id} is owned by {@code schedule_id}
     *
     * @param object_id object to check
     * @param schedule_id schedule to check
     * @return true if {@code schedule_id} is owner of object {@code object_id}

    @Override
    public boolean checkIsOwner(long object_id, long schedule_id) throws SQLException {
        PreparedStatement ps = connectImpl.prepareStatement(
                "SELECT * FROM schedule WHERE schedule_id = ? AND owner_id = ?");
        ps.setLong(1, object_id);
        ps.setLong(2, schedule_id);
        ResultSet rs = ps.executeQuery();
        try {
            return rs.next();
        } finally {
            rs.close();
            ps.close();
        }
    }*/

}