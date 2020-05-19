package storage.jdbc;

import storage.IStorage;
import web.model.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC storage access to {@code model.Service} objects
 */
public class ServiceStorage implements IStorage<Service> {

    /**
     * Connection fast access variable
     */
    Connection dbConn;

    public ServiceStorage(Connection connection) {
        dbConn = connection;
    }

    /**
     * Get {@code model.Service} object from storage by {@code service_id}
     *
     * @param object_id object identifier
     * @return {@code model.Service} object
     * @throws Exception on error accessing storage
     */
    public Service select(long object_id) throws Exception {
        PreparedStatement ps = dbConn.prepareStatement(
                "SELECT * FROM service WHERE service_id = ?");
        ps.setLong(1, object_id);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            Service r = new Service();
            r.service_id = rs.getLong("service_id");
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
     * Set {@code model.Service} object to storage by {@code service.service_id}
     *
     * @param service updated object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean update(Service service) throws Exception {
        PreparedStatement ps = dbConn.prepareStatement(
                "UPDATE service SET name = ?, description = ?, image_id = ?, duration = ?, cost = ?" +
                        " WHERE service_id = ?");
        try {
            ps.setString(1, service.name);
            ps.setString(2, service.description);
            ps.setLong(3, service.image_id);
            ps.setInt(4, service.duration);
            ps.setInt(5, service.cost);
            ps.setLong(6, service.service_id);

            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        } finally {
            ps.close();
        }
    }

    /**
     * Create new {@code model.Service} object in storage
     * {@code service.service_id} will be update to new value
     *
     * @param service new object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean insert(Service service) throws Exception {
        String resultColumns[] = new String[]{"service_id"};
        PreparedStatement ps = dbConn.prepareStatement(
                "INSERT INTO service (service_id, name, description, image_id, duration, cost)" +
                        "VALUES (seq_service_id.nextval, ?, ?, ?, ?, ?)", resultColumns);
        try {
            ps.setString(1, service.name);
            ps.setString(2, service.description);
            ps.setLong(3, service.image_id);
            ps.setInt(4, service.duration);
            ps.setInt(5, service.cost);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 1) {
                java.sql.ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    service.service_id = generatedKeys.getLong(1);
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
                "DELETE FROM service WHERE service_id = ?");
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
    public List<Service> selectAllQuick() throws Exception {
        PreparedStatement ps = dbConn.prepareStatement(
                "SELECT service_id, name FROM service ORDER BY name");
        ResultSet rs = ps.executeQuery();
        List<Service> list = new ArrayList<Service>();
        try {
            while (rs.next()) {
                Service r = new Service();
                r.service_id = rs.getLong("service_id");
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