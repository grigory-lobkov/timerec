package storage;

import java.util.List;

public interface IStorage<T> {

    /**
     * Get model.Service object from storage by {@code service_id}
     *
     * @param object_id object to search
     * @return model.Service object
     * @throws Exception on error accessing storage
     */
    T select(long object_id) throws Exception;

    /**
     * Get model.Service object from storage by {@code service_id}
     *
     * @param filter object to search
     * @return model.Service object
     * @throws Exception on error accessing storage
     */
    T select(String filter) throws Exception;

    /**
     * Set model.Service object to storage by {@code service.service_id}
     *
     * @param service updated object
     * @return true on success
     * @throws Exception on error accessing storage
     */
    boolean update(T service) throws Exception;


    /**
     * Create new model.Service object in storage
     * {@code service.service_id} will be update to new value
     *
     * @param service new object
     * @return true on success
     * @throws Exception on error accessing storage
     */
    boolean insert(T service) throws Exception;


    /**
     * Delete model.Service object from storage by {@code service_id}
     *
     * @param object_id
     * @return true on success
     * @throws Exception on error accessing storage
     */
    boolean delete(long object_id) throws Exception;

    /**
     * Generates simple list of data
     *
     * @return list
     * @throws Exception on error accessing storage
     */
    List<T> selectAllQuick() throws Exception;
}