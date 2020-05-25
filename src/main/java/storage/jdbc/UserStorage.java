package storage.jdbc;

import storage.IStorage;
import web.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC storage access to {@code model.User} objects
 */
public class UserStorage implements IStorage<User> {

    /**
     * Connection fast access variable
     */
    Connection dbConn;

    public UserStorage(Connection connection) {
        dbConn = connection;
    }

    /**
     * Get {@code model.User} object from storage by {@code user_id}
     *
     * @param object_id object identifier
     * @return {@code model.User} object
     * @throws Exception on error accessing storage
     */
    public User select(long object_id) throws Exception {
        PreparedStatement ps = dbConn.prepareStatement(
                "SELECT * FROM user WHERE user_id = ?");
        ps.setLong(1, object_id);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            User r = new User();
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
        }
    }

    /**
     * Get {@code model.User} object from storage by {@code email}
     *
     * @param filter object
     * @return {@code model.User} object
     * @throws Exception on error accessing storage
     */
    public User select(String filter) throws Exception {
        PreparedStatement ps = dbConn.prepareStatement(
                "SELECT * FROM user WHERE email = ?");
        ps.setString(1, filter);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            User r = new User();
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
        }
    }

    /**
     * Set {@code model.User} object to storage by {@code user.user_id}
     *
     * @param user updated object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean update(User user) throws Exception {
        PreparedStatement ps = dbConn.prepareStatement(
                "UPDATE user SET role_id = ?, name = ?, tz_id = ?, email = ?, password = ?, image_id = ?, owner_id = ?" +
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
        }
    }

    /**
     * Create new {@code model.User} object in storage
     * {@code user.user_id} will be update to new value
     *
     * @param user new object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean insert(User user) throws Exception {
        String resultColumns[] = new String[]{"user_id"};
        PreparedStatement ps = dbConn.prepareStatement(
                "INSERT INTO user (user_id, role_id, name, tz_id, email, password, image_id, owner_id)" +
                        "VALUES (seq_user_id.nextval, ?, ?, ?, ?, ?, ?, ?)", resultColumns);
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
        }
    }

    /**
     * Delete {@code model.Service} object from storage by {@code service_id}
     *
     * @param object_id
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public boolean delete(long object_id) throws Exception {
        PreparedStatement ps = dbConn.prepareStatement(
                "DELETE FROM user WHERE user_id = ?");
        ps.setLong(1, object_id);
        try {
            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        } finally {
            ps.close();
        }
    }

    /**
     * Reads only {@code service_id} and {@code name} from list of services
     *
     * @return list of shortened {@code model.Service} objects
     * @throws Exception
     */
    @Override
    public List<User> selectAllQuick() throws Exception {
        PreparedStatement ps = dbConn.prepareStatement(
                "SELECT user_id, name FROM user ORDER BY name");
        ResultSet rs = ps.executeQuery();
        List<User> list = new ArrayList<User>();
        try {
            while (rs.next()) {
                User r = new User();
                r.user_id = rs.getLong("user_id");
                r.name = rs.getString("name");
                list.add(r);
            }
        } finally {
            rs.close();
            ps.close();
        }
        return list;
    }

}