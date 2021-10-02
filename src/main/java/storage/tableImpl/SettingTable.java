package storage.tableImpl;

import model.SettingRow;
import storage.IConnectionPool;
import storage.ITable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC storage access to {@code model.SettingRow} objects
 */
public class SettingTable implements ITable<SettingRow> {

    private final IConnectionPool pool;
    private final String preSeqNextval;
    private final String postSeqNextval;
    private final String preSeqCurrval;
    private final String postSeqCurrval;
    private final String fromDual;


    public SettingTable(IConnectionPool connection) {
        pool = connection;
        preSeqNextval = pool.preSeqNextval();
        postSeqNextval = pool.postSeqNextval();
        preSeqCurrval = pool.preSeqCurrval();
        postSeqCurrval = pool.postSeqCurrval();
        fromDual = pool.fromDual();
    }


    /**
     * Get {@code model.SettingRow} object from storage by {@code setting_id}
     *
     * @param object_id object identifier
     * @return {@code model.SettingRow} object
     * @throws Exception on error accessing storage
     */
    public SettingRow select(long object_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM setting WHERE setting_id = ?");
        ps.setLong(1, object_id);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            SettingRow r = new SettingRow();
            r.setting_id = rs.getLong("setting_id");
            r.alias = rs.getString("alias");
            r.name = rs.getString("name");
            r.description = rs.getString("description");
            r.value = rs.getString("value");
            r.service_id = rs.getLong("service_id");
            r.owner_id = rs.getLong("owner_id");
            return r;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Get {@code model.SettingRow} object from storage by {@code email}
     *
     * @param filter object
     * @return {@code model.SettingRow} object
     * @throws Exception on error accessing storage
     */
    public SettingRow select(String filter) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM setting WHERE alias = ?");
        ps.setString(1, filter);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            SettingRow r = new SettingRow();
            r.setting_id = rs.getLong("setting_id");
            r.alias = rs.getString("alias");
            r.name = rs.getString("name");
            r.description = rs.getString("description");
            r.value = rs.getString("value");
            r.service_id = rs.getLong("service_id");
            r.owner_id = rs.getLong("owner_id");
            return r;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Set {@code model.SettingRow} object to storage by {@code setting.setting_id}
     *
     * @param object updated object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean update(SettingRow object) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE setting SET alias = ?, name = ?, description = ?, value = ?, service_id = ?, owner_id = ?" +
                        " WHERE setting_id = ?");
        try {
            ps.setString(1, object.alias);
            ps.setString(2, object.name);
            ps.setString(3, object.description);
            ps.setString(4, object.value);
            ps.setLong(5, object.service_id);
            ps.setLong(6, object.owner_id);
            ps.setLong(7, object.setting_id);

            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Create new {@code model.SettingRow} object in storage
     * {@code setting.setting_id} will be updated to new value
     *
     * @param object new object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean insert(SettingRow object) throws Exception {
        Connection conn = pool.connection();
        String resultColumns[] = new String[]{"setting_id"};
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO setting (setting_id, alias, name, description, value, service_id, owner_id)" +
                        "VALUES (" + preSeqNextval + "seq_setting_id" + postSeqNextval + ", ?, ?, ?, ?, ?, ?)", resultColumns);
        try {
            ps.setString(1, object.alias);
            ps.setString(2, object.name);
            ps.setString(3, object.description);
            ps.setString(4, object.value);
            ps.setLong(5, object.service_id);
            ps.setLong(6, object.owner_id);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    object.setting_id = generatedKeys.getLong(1);
                } else { // when getGeneratedKeys() is not supported by DBMS
                    PreparedStatement csps = conn.prepareStatement("SELECT " + preSeqCurrval + "seq_setting_id" + postSeqCurrval + " " + fromDual);
                    ResultSet csrs = csps.executeQuery();
                    if (csrs.next()) {
                        object.setting_id = csrs.getLong(1);
                    }
                }
            }
            return affectedRows == 1;
        } finally {
            ps.close();
            conn.close();
        }
    }

    /**
     * Delete {@code model.SettingRow} object from storage by {@code setting_id}
     *
     * @param object_id
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public boolean delete(long object_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM setting WHERE setting_id = ?");
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
     * Reads only {@code setting_id} and {@code name} from list of settings
     *
     * @return list of shortened {@code model.SettingRow} objects
     * @throws Exception
     */
    @Override
    public List<SettingRow> select() throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM setting");
        ResultSet rs = ps.executeQuery();
        List<SettingRow> list = new ArrayList<SettingRow>();
        try {
            while (rs.next()) {
                SettingRow r = new SettingRow();
                r.setting_id = rs.getLong("setting_id");
                r.alias = rs.getString("alias");
                r.name = rs.getString("name");
                r.description = rs.getString("description");
                r.value = rs.getString("value");
                r.service_id = rs.getLong("service_id");
                r.owner_id = rs.getLong("owner_id");
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
     * Check model.SettingRow object {@code object_id} is owned by {@code setting_id}
     *
     * @param object_id object to check
     * @param setting_id setting to check
     * @return true if {@code setting_id} is owner of object {@code object_id}

    @Override
    public boolean checkIsOwner(long object_id, long setting_id) throws SQLException {
        PreparedStatement ps = connectImpl.prepareStatement(
                "SELECT * FROM setting WHERE setting_id = ? AND owner_id = ?");
        ps.setLong(1, object_id);
        ps.setLong(2, setting_id);
        ResultSet rs = ps.executeQuery();
        try {
            return rs.next();
        } finally {
            rs.close();
            ps.close();
        }
    }*/

}
