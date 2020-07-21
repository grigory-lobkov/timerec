package storage.tableImpl;

import model.ImageRow;
import storage.IConnectionPool;
import storage.ITable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * JDBC storage access to {@code model.ImageRow} objects
 */
public class ImageTable implements ITable<ImageRow> {

    private IConnectionPool pool;
    private String preSeqNextval;
    private String postSeqNextval;

    public ImageTable(IConnectionPool connection) {
        pool = connection;
        preSeqNextval = pool.preSeqNextval();
        postSeqNextval = pool.postSeqNextval();
    }


    /**
     * Get {@code model.ImageRow} object from storage by {@code image_id}
     *
     * @param object_id object identifier
     * @return {@code model.ImageRow} object
     * @throws Exception on error accessing storage
     */
    public ImageRow select(long object_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM image WHERE image_id = ?");
        ps.setLong(1, object_id);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            ImageRow r = new ImageRow();
            r.image_id = rs.getLong("image_id");
            r.filename = rs.getString("filename");
            r.altname = rs.getString("altname");
            r.width = rs.getInt("width");
            r.height = rs.getInt("height");
            r.bitmap = rs.getString("bitmap");
            return r;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Get {@code model.ImageRow} object from storage by {@code filename}
     *
     * @param filter object
     * @return {@code model.ImageRow} object
     * @throws Exception on error accessing storage
     */
    public ImageRow select(String filter) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM image WHERE filename = ?");
        ps.setString(1, filter);
        ResultSet rs = ps.executeQuery();
        try {
            if (!rs.next()) return null;

            ImageRow r = new ImageRow();
            r.image_id = rs.getLong("image_id");
            r.filename = rs.getString("filename");
            r.altname = rs.getString("altname");
            r.width = rs.getInt("width");
            r.height = rs.getInt("height");
            r.bitmap = rs.getString("bitmap");
            return r;
        } finally {
            rs.close();
            ps.close();
            conn.close();
        }
    }


    /**
     * Set {@code model.ImageRow} object to storage by {@code image.image_id}
     *
     * @param image updated object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean update(ImageRow image) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE image SET filename = ?, altname = ?, width = ?, height = ?, bitmap = ?" +
                        " WHERE image_id = ?");
        try {
            ps.setString(1, image.filename);
            ps.setString(2, image.altname);
            ps.setInt(3, image.width);
            ps.setInt(4, image.height);
            ps.setString(5, image.bitmap);
            ps.setLong(6, image.image_id);

            int affectedRows = ps.executeUpdate();
            return affectedRows == 1;
        } finally {
            ps.close();
            conn.close();
        }
    }


    /**
     * Create new {@code model.ImageRow} object in storage
     * {@code image.image_id} will be updated to new value
     *
     * @param image new object
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    public boolean insert(ImageRow image) throws Exception {
        Connection conn = pool.connection();
        String resultColumns[] = new String[]{"image_id"};
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO image (image_id, filename, altname, width, height, bitmap)" +
                        "VALUES (" + preSeqNextval + "seq_image_id" + postSeqNextval + ", ?, ?, ?, ?, ?)", resultColumns);
        try {
            ps.setString(1, image.filename);
            ps.setString(2, image.altname);
            ps.setInt(3, image.width);
            ps.setInt(4, image.height);
            ps.setString(5, image.bitmap);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 1) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    image.image_id = generatedKeys.getLong(1);
                }
            }
            return affectedRows == 1;
        } finally {
            ps.close();
            conn.close();
        }
    }

    /**
     * Delete {@code model.ImageRow} object from storage by {@code image_id}
     *
     * @param object_id
     * @return {@code true} on success
     * @throws Exception on error accessing storage
     */
    @Override
    public boolean delete(long object_id) throws Exception {
        Connection conn = pool.connection();
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM image WHERE image_id = ?");
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
     * Reads only {@code image_id} and {@code name} from list of images
     *
     * @return list of shortened {@code model.ImageRow} objects
     * @throws Exception
     */
    @Override
    public List<ImageRow> select() throws Exception {
        throw new RuntimeException("ImageTable.select() not implemented.");
    }


    /*
     * Check model.ImageRow object {@code object_id} is owned by {@code image_id}
     *
     * @param object_id object to check
     * @param image_id image to check
     * @return true if {@code image_id} is owner of object {@code object_id}

    @Override
    public boolean checkIsOwner(long object_id, long image_id) throws SQLException {
        PreparedStatement ps = connectImpl.prepareStatement(
                "SELECT * FROM image WHERE image_id = ? AND owner_id = ?");
        ps.setLong(1, object_id);
        ps.setLong(2, image_id);
        ResultSet rs = ps.executeQuery();
        try {
            return rs.next();
        } finally {
            rs.close();
            ps.close();
        }
    }*/

}