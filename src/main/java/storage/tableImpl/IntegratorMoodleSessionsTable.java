package storage.tableImpl;

import integration.Integrator;
import model.IntegratorMoodleSessionsRow;
import storage.IConnectionPool;
import storage.ITable;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * JDBC storage access to {@code model.IntegratorMoodleSessionsRow} objects
 */
public class IntegratorMoodleSessionsTable implements ITable<IntegratorMoodleSessionsRow> {

    private static final String TABLE_NAME = Integrator.getProperty("INTEGRATOR_MOODLEDB_SESSIONS_TABLE_NAME");
    private static final String SELECT_FILTER_QUERY = "SELECT * FROM " + TABLE_NAME + " WHERE sid = ?";

    private final IConnectionPool pool;

    public IntegratorMoodleSessionsTable(IConnectionPool connection) {
        pool = connection;
    }

    public IntegratorMoodleSessionsRow select(long objectId) throws Exception {
        throw new NotImplementedException();
    }

    public IntegratorMoodleSessionsRow select(String filter) throws Exception {
        try (Connection conn = pool.connection();
             PreparedStatement ps = conn.prepareStatement(SELECT_FILTER_QUERY)
        ) {
            ps.setString(1, filter);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                IntegratorMoodleSessionsRow r = new IntegratorMoodleSessionsRow();
                r.id = rs.getLong("id");
                r.state = rs.getLong("state");
                r.sid = rs.getString("sid");
                r.userid = rs.getLong("userid");
                r.sessdata = rs.getString("sessdata");
                r.timecreated = rs.getLong("timecreated");
                r.timemodified = rs.getLong("timemodified");
                r.firstip = rs.getString("firstip");
                r.lastip = rs.getString("lastip");
                return r;
            }
        }
    }

    public boolean update(IntegratorMoodleSessionsRow access) throws Exception {
            throw new NotImplementedException();
    }

    public boolean insert(IntegratorMoodleSessionsRow access) throws Exception {
            throw new NotImplementedException();
    }

    public boolean delete(long objectId) throws Exception {
            throw new NotImplementedException();
    }

    public List<IntegratorMoodleSessionsRow> select() throws Exception {
        throw new NotImplementedException();
    }

}
