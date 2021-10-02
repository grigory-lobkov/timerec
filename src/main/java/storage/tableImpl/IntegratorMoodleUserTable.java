package storage.tableImpl;

import integration.Integrator;
import model.IntegratorMoodleUserRow;
import storage.IConnectionPool;
import storage.ITable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * JDBC storage access to {@code model.IntegratorMoodleUserRow} objects
 */
public class IntegratorMoodleUserTable implements ITable<IntegratorMoodleUserRow> {

    private static final String TABLE_NAME = Integrator.getProperty("INTEGRATOR_MOODLEDB_USER_TABLE_NAME");
    private static final String SELECT_QUERY = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
    private static final String SELECT_FILTER_QUERY = "SELECT * FROM " + TABLE_NAME + " WHERE mnethostid = 1 AND username = ?";

    private final IConnectionPool pool;

    public IntegratorMoodleUserTable(IConnectionPool connection) {
        pool = connection;
    }

    public IntegratorMoodleUserRow select(long objectId) throws Exception {
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(SELECT_QUERY)
        ) {
            ps.setLong(1, objectId);
            return extractRow(ps);
        }
    }

    private IntegratorMoodleUserRow extractRow(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return null;
            IntegratorMoodleUserRow r = new IntegratorMoodleUserRow();
            r.id = rs.getLong("id");
            r.confirmed = rs.getInt("confirmed");
            r.deleted = rs.getInt("deleted");
            r.suspended = rs.getInt("suspended");
            r.mnethostid = rs.getLong("mnethostid");
            r.username = rs.getString("username");
            r.firstname = rs.getString("firstname");
            r.lastname = rs.getString("lastname");
            r.email = rs.getString("email");
            r.emailstop = rs.getInt("emailstop");
            r.lang = rs.getString("lang");
            r.timezone = rs.getString("timezone");
            r.firstaccess = rs.getLong("firstaccess");
            r.lastaccess = rs.getLong("lastaccess");
            r.lastlogin = rs.getLong("lastlogin");
            r.currentlogin = rs.getLong("currentlogin");
            r.lastip = rs.getString("lastip");
            r.timecreated = rs.getLong("timecreated");
            r.timemodified = rs.getLong("timemodified");
            return r;
        }
    }

    public IntegratorMoodleUserRow select(String filter) throws Exception {
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(SELECT_FILTER_QUERY)
        ) {
            ps.setString(1, filter);
            return extractRow(ps);
        }
    }

    public boolean update(IntegratorMoodleUserRow access) throws Exception {
            throw new NotImplementedException();
    }

    public boolean insert(IntegratorMoodleUserRow access) throws Exception {
            throw new NotImplementedException();
    }

    public boolean delete(long objectId) throws Exception {
            throw new NotImplementedException();
    }

    public List<IntegratorMoodleUserRow> select() throws Exception {
        throw new NotImplementedException();
    }

}
