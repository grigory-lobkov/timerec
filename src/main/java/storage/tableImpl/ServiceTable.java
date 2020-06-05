package storage.tableImpl;

import model.ServiceRow;
import storage.IConnectionPool;
import storage.ITable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC storage access to {@code model.ServiceRow} objects
 */
public class ServiceTable implements ITable<ServiceRow> {

    /**
     * Connection fast access variable
     */
    IConnectionPool pool;


    public ServiceTable(IConnectionPool connection) {
        pool = connection;
    }


    /**
     * Get {@code model.ServiceRow} object from storage by {@code service_id}
     *
     * @param object_id object identifier
     * @return {@code model.ServiceRow} object
     * @throws Exception on error accessing storage
     */
    public ServiceRow select(long object_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM service WHERE service_id = ?");
        ps.setLong(1, object_id);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            ServiceRow r = new ServiceRow();
            r.service_id = rs.getLong("service_id");
            r.name = rs.getString("name");
            r.description = rs.getString("description");
            r.image_id = rs.getLong("image_id");
            r.duration = rs.getInt("duration");
            r.cost = rs.getInt("cost");
            r.owner_id = rs.getLong("owner_id");
            return r;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Get {@code model.ServiceRow} object from storage by {@code filter}
     *
     * @param filter
     * @return {@code model.ServiceRow} object
     * @throws Exception on error accessing storage
     */
    public ServiceRow select(String filter) throws Exception {
        throw new RuntimeException("ServiceTable.select(String filter) not implemented.");
    }


    /**
     * Set {@code model.ServiceRow} object to storage by {@code service.service_id}
     *
     * @param service updated object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean update(ServiceRow service) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE service SET name = ?, description = ?, image_id = ?, duration = ?, cost = ?, owner_id = ?" +
                        " WHERE service_id = ?");
        try {
            ps.setString(1, service.name);
            ps.setString(2, service.description);
            ps.setLong(3, service.image_id);
            ps.setInt(4, service.duration);
            ps.setInt(5, service.cost);
            ps.setLong(6, service.owner_id);
            ps.setLong(7, service.service_id);

            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Create new {@code model.ServiceRow} object in storage
     * {@code service.service_id} will be update to new value
     *
     * @param service new object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean insert(ServiceRow service) throws Exception {
        Connection conn = pool.connection();
        String resultColumns[] = new String[]{"service_id"};
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO service (service_id, name, description, image_id, duration, cost, owner_id)" +
                        "VALUES (seq_service_id.nextval, ?, ?, ?, ?, ?, ?)", resultColumns);
        try {
            ps.setString(1, service.name);
            ps.setString(2, service.description);
            ps.setLong(3, service.image_id);
            ps.setInt(4, service.duration);
            ps.setInt(5, service.cost);
            ps.setLong(6, service.image_id);

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
            conn.close();
        }
    }


    /**
     * Delete {@code model.ServiceRow} object from storage by {@code service_id}
     *
     * @param object_id
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public boolean delete(long object_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM service WHERE service_id = ?");
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
     * Reads only {@code service_id} and {@code name} from list of services
     *
     * @return list of shortened {@code model.ServiceRow} objects
     * @throws Exception
     */
    @Override
    public List<ServiceRow> selectAllQuick() throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT service_id, name FROM service ORDER BY name");
        ResultSet rs = ps.executeQuery();
        List<ServiceRow> list = new ArrayList<ServiceRow>();
        try {
            while (rs.next()) {
                ServiceRow r = new ServiceRow();
                r.service_id = rs.getLong("service_id");
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
     * Check model.ServiceRow object {@code object_id} is owned by {@code user_id}
     *
     * @param object_id object to check
     * @param user_id user to check
     * @return true if {@code user_id} is owner of object {@code object_id}

    @Override
    public boolean checkIsOwner(long object_id, long user_id) throws SQLException {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM service WHERE service_id = ? AND owner_id = ?");
        ps.setLong(1, object_id);
        ps.setLong(2, user_id);
        ResultSet rs = ps.executeQuery();
        try {
            return rs.next();
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }*/

}