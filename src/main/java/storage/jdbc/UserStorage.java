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
            r.name = rs.getString("name");
            r.description = rs.getString("description");
            r.image_id = rs.getLong("image_id");
            r.duration = rs.getInt("duration");
            r.cost = rs.getInt("cost");
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
            r.name = rs.getString("name");
            r.description = rs.getString("description");
            r.image_id = rs.getLong("image_id");
            r.duration = rs.getInt("duration");
            r.cost = rs.getInt("cost");
            return r;
        } finally {
            rs.close();
            ps.close();
        }
    }

    /**
     * Set {@code model.User} object to storage by {@code user.user_id}
     *
     * @param service updated object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean update(User service) throws Exception {
        PreparedStatement ps = dbConn.prepareStatement(
                "UPDATE user SET name = ?, description = ?, image_id = ?, duration = ?, cost = ?" +
                        " WHERE user_id = ?");
        try {
            ps.setString(1, service.name);
            ps.setString(2, service.description);
            ps.setLong(3, service.image_id);
            ps.setInt(4, service.duration);
            ps.setInt(5, service.cost);
            ps.setLong(6, service.user_id);

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
     * @param service new object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean insert(User service) throws Exception {
        String resultColumns[] = new String[]{"user_id"};
        PreparedStatement ps = dbConn.prepareStatement(
                "INSERT INTO user (user_id, name, description, image_id, duration, cost)" +
                        "VALUES (seq_user_id.nextval, ?, ?, ?, ?, ?)", resultColumns);
        try {
            ps.setString(1, service.name);
            ps.setString(2, service.description);
            ps.setLong(3, service.image_id);
            ps.setInt(4, service.duration);
            ps.setInt(5, service.cost);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    service.user_id = generatedKeys.getLong(1);
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