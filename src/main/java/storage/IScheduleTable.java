package storage;

import model.ScheduleRow;

import java.sql.Timestamp;
import java.util.List;

public interface IScheduleTable<T> {

    /**
     * Get model.* object from storage by {@code id}
     *
     * @param object_id object to search
     * @return model.* object
     * @throws Exception on error accessing storage
     */
    T select(long object_id) throws Exception;


    /**
     * Set model.* object to storage by {@code id}
     *
     * @param object updated object
     * @return true on success
     * @throws Exception on error accessing storage
     */
    boolean update(T object) throws Exception;


    /**
     * Create new model.* object in storage
     * {@code id} will be update to new value
     *
     * @param object new object
     * @return true on success
     * @throws Exception on error accessing storage
     */
    boolean insert(T object) throws Exception;


    /**
     * Delete model.* object from storage by {@code id}
     *
     * @param object_id
     * @return true on success
     * @throws Exception on error accessing storage
     */
    boolean delete(long object_id) throws Exception;


    List<ScheduleRow> selectByService(long service_id, Timestamp date_from, Timestamp date_to) throws Exception;


    List<ScheduleRow> selectByUser(long user_id, Timestamp date_from, Timestamp date_to) throws Exception;

    List<ScheduleRow> selectByDate(Timestamp date_from, Timestamp date_to) throws Exception;
}