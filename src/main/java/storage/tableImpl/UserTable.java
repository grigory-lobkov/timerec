package storage.tableImpl;

import model.UserRow;
import storage.IConnectionPool;
import storage.ITable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC storage access to {@code model.UserRow} objects
 */
public class UserTable implements ITable<UserRow> {

    private IConnectionPool pool;
    private String preSeqNextval;
    private String postSeqNextval;

    public UserTable(IConnectionPool connection) {
        pool = connection;
        preSeqNextval = pool.preSeqNextval();
        postSeqNextval = pool.postSeqNextval();
    }


    /**
     * Get {@code model.UserRow} object from storage by {@code user_id}
     *
     * @param object_id object identifier
     * @return {@code model.UserRow} object
     * @throws Exception on error accessing storage
     */
    public UserRow select(long object_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT u.*, (SELECT utc_offset FROM tz WHERE tz_id = u.tz_id) AS tz_utc_offset" +
                        " FROM users u WHERE user_id = ?");
        ps.setLong(1, object_id);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            UserRow r = new UserRow();
            r.user_id = rs.getLong("user_id");
            r.role_id = rs.getLong("role_id");
            r.name = rs.getString("name");
            r.tz_id = rs.getLong("tz_id");
            r.email = rs.getString("email");
            r.password = rs.getString("password");
            r.image_id = rs.getLong("image_id");
            r.owner_id = rs.getLong("owner_id");
            r.tz_utc_offset = rs.getInt("tz_utc_offset");
            return r;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Get {@code model.UserRow} object from storage by {@code email}
     *
     * @param filter object
     * @return {@code model.UserRow} object
     * @throws Exception on error accessing storage
     */
    public UserRow select(String filter) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT u.*, (SELECT utc_offset FROM tz WHERE tz_id = u.tz_id) AS tz_utc_offset" +
                        " FROM users u WHERE email = ?");
        ps.setString(1, filter);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            UserRow r = new UserRow();
            r.user_id = rs.getLong("user_id");
            r.role_id = rs.getLong("role_id");
            r.name = rs.getString("name");
            r.tz_id = rs.getLong("tz_id");
            r.email = rs.getString("email");
            r.password = rs.getString("password");
            r.image_id = rs.getLong("image_id");
            r.owner_id = rs.getLong("owner_id");
            return r;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Set {@code model.UserRow} object to storage by {@code user.user_id}
     *
     * @param user updated object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean update(UserRow user) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET role_id = ?, name = ?, tz_id = ?, email = ?, password = ?, image_id = ?, owner_id = ?" +
                        " WHERE user_id = ?");
        try {
            ps.setLong(1, user.role_id);
            ps.setString(2, user.name);
            ps.setLong(3, user.tz_id);
            ps.setString(4, user.email);
            ps.setString(5, user.password);
            ps.setLong(6, user.image_id);
            ps.setLong(7, user.owner_id);
            ps.setLong(8, user.user_id);

            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Create new {@code model.UserRow} object in storage
     * {@code user.user_id} will be updated to new value
     *
     * @param user new object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean insert(UserRow user) throws Exception {
        Connection conn = pool.connection();
        String resultColumns[] = new String[]{"user_id"};
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (user_id, role_id, name, tz_id, email, password, image_id, owner_id)" +
                        "VALUES (" + preSeqNextval + "seq_users_id" + postSeqNextval + ", ?, ?, ?, ?, ?, ?, ?)", resultColumns);
        try {
            ps.setLong(1, user.role_id);
            ps.setString(2, user.name);
            ps.setLong(3, user.tz_id);
            ps.setString(4, user.email);
            ps.setString(5, user.password);
            ps.setLong(6, user.image_id);
            ps.setLong(7, user.owner_id);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.user_id = generatedKeys.getLong(1);
                }
            }
            return affectedRows == 1;
        } finally {
            ps.close();
            conn.close();
        }
    }

    /**
     * Delete {@code model.UserRow} object from storage by {@code user_id}
     *
     * @param object_id
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public boolean delete(long object_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM users WHERE user_id = ?");
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
     * Reads only {@code user_id} and {@code name} from list of users
     *
     * @return list of shortened {@code model.UserRow} objects
     * @throws Exception
     */
    @Override
    public List<UserRow> select() throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT user_id, name FROM users ORDER BY name");
        ResultSet rs = ps.executeQuery();
        List<UserRow> list = new ArrayList<UserRow>();
        try {
            while (rs.next()) {
                UserRow r = new UserRow();
                r.user_id = rs.getLong("user_id");
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
     * Check model.UserRow object {@code object_id} is owned by {@code user_id}
     *
     * @param object_id object to check
     * @param user_id user to check
     * @return true if {@code user_id} is owner of object {@code object_id}

    @Override
    public boolean checkIsOwner(long object_id, long user_id) throws SQLException {
        PreparedStatement ps = connectImpl.prepareStatement(
                "SELECT * FROM user WHERE user_id = ? AND owner_id = ?");
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