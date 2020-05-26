package storage;

import java.util.List;

public interface IStorage<T> {

    /**
     * Get model.* object from storage by {@code id}
     *
     * @param object_id object to search
     * @return model.* object
     * @throws Exception on error accessing storage
     */
    T select(long object_id) throws Exception;

    /**
     * Get model.* object from storage by {@code id}
     *
     * @param filter object to search
     * @return model.* object
     * @throws Exception on error accessing storage
     */
    T select(String filter) throws Exception;

    /**
     * Set model.* object to storage by {@code id}
     *
     * @param service updated object
     * @return true on success
     * @throws Exception on error accessing storage
     */
    boolean update(T service) throws Exception;


    /**
     * Create new model.* object in storage
     * {@code id} will be update to new value
     *
     * @param service new object
     * @return true on success
     * @throws Exception on error accessing storage
     */
    boolean insert(T service) throws Exception;


    /**
     * Delete model.* object from storage by {@code id}
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

    /*
     * Check model.* object {@code object_id} is owned by {@code user_id}
     *
     * @param object_id object to check
     * @param user_id user to check
     * @return true if {@code user_id} is owner of object {@code object_id}

    boolean checkIsOwner(long object_id, long user_id) throws Exception;*/

}