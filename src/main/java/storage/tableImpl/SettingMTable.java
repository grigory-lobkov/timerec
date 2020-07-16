package storage.tableImpl;

import model.SettingRow;
import storage.IConnectionPool;
import storage.IMultiRowTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC storage access to {@code model.SettingRow} objects
 * Deprecated?
 *
 * Parent filter field = "service_id"
 */
public class SettingMTable implements IMultiRowTable<SettingRow> {

    private IConnectionPool pool;
    private String preSeqNextval;
    private String postSeqNextval;


    public SettingMTable(IConnectionPool connection) {
        pool = connection;
        preSeqNextval = pool.preSeqNextval();
        postSeqNextval = pool.postSeqNextval();
    }


    /**
     * Get {@code model.SettingRow} object from storage by {@code setting_id}
     *
     * @param ids object identifiers
     * @return list of {@code model.SettingRow} objects
     * @throws Exception on error accessing storage
     */
    @Override
    public List<SettingRow> select(List<Long> ids) throws Exception {
        if (ids.size() == 0)
            return new ArrayList<>();

        StringBuilder commaList = new StringBuilder(ids.size() * 7);
        for (Long id : ids) {
            commaList.append((commaList.length() == 0 ? "" : ",") + id);
        }

        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM setting WHERE setting_id IN (" + commaList + ")");
        ResultSet rs = ps.executeQuery();

        List<SettingRow> result = new ArrayList<>();
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
                result.add(r);
            }
            return result;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Get {@code model.SettingRow} object from storage by {@code setting_id}
     *
     * @param parent_id object identifier
     * @return {@code model.SettingRow} object
     * @throws Exception on error accessing storage
     */
    @Override
    public List<SettingRow> select(long parent_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM setting WHERE service_id = ?");
        ps.setLong(1, parent_id);
        ResultSet rs = ps.executeQuery();

        List<SettingRow> result = new ArrayList<SettingRow>();
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
                result.add(r);
            }
            return result;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Set {@code model.SettingRow} object to storage by {@code setting.setting_id}
     *
     * @param objects updated objects
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public int update(List<SettingRow> objects) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE setting SET alias = ?, name = ?, description = ?, value = ?, service_id = ?, owner_id = ?" +
                        " WHERE setting_id = ?");

        int affectedRows = 0;
        try {
            for (SettingRow object : objects) {
                ps.setString(1, object.alias);
                ps.setString(2, object.name);
                ps.setString(3, object.description);
                ps.setString(4, object.value);
                ps.setLong(5, object.service_id);
                ps.setLong(6, object.owner_id);
                ps.setLong(7, object.setting_id);

                affectedRows += ps.executeUpdate();
            }
            return affectedRows;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Create new {@code model.SettingRow} object in storage
     * {@code setting.setting_id} will be update to new value
     *
     * @param objects new objects list
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public int insert(List<SettingRow> objects) throws Exception {
        Connection conn = pool.connection();
        String resultColumns[] = new String[]{"setting_id"};
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO setting (setting_id, alias, name, description, value, service_id, owner_id)" +
                        "VALUES (" + preSeqNextval + "seq_setting_id" + postSeqNextval + ", ?, ?, ?, ?, ?, ?)", resultColumns);

        int affectedRows = 0;
        try {
            for (SettingRow object : objects) {
                ps.setString(1, object.alias);
                ps.setString(2, object.name);
                ps.setString(3, object.description);
                ps.setString(4, object.value);
                ps.setLong(5, object.service_id);
                ps.setLong(6, object.owner_id);

                if (ps.executeUpdate() == 1) {
                    affectedRows++;
                    ResultSet generatedKeys = ps.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        object.setting_id = generatedKeys.getLong(1);
                    }
                }
            }
            return affectedRows;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Delete {@code model.SettingRow} object from storage by {@code parent_id}
     *
     * @param parent_id filter field
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public int delete(long parent_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM setting WHERE service_id = ?");
        ps.setLong(1, parent_id);
        try {
            int affectedRows = ps.executeUpdate();
            return affectedRows;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Delete {@code model.SettingRow} object from storage by {@code ids}
     *
     * @param ids object identifiers
     * @return true on success
     * @throws Exception on error accessing storage
     */
    @Override
    public int delete(List<Long> ids) throws Exception {
        if (ids.size() == 0)
            return 0;

        StringBuilder commaList = new StringBuilder(ids.size() * 7);
        for (Long id : ids) {
            commaList.append((commaList.length() == 0 ? "" : ",") + id);
        }
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM setting WHERE setting_id IN (" + commaList + ")");
        try {
            int affectedRows = ps.executeUpdate();
            return affectedRows;
        } finally {
            ps.close();
            conn.close();
        }
    }

}