package storage;

import java.util.List;

public interface IMultiRowTable<T> {


    /**
     * Get model.* objects from storage by {@code ids}
     *
     * @param ids identifiers of rows
     * @return model.* object
     * @throws Exception on error accessing storage
     */
    List<T> select(List<Long> ids) throws Exception;


    /**
     * Get model.* objects from storage by {@code parent_id}
     *
     * @param parent_id all rows of parent object
     * @return model.* object
     * @throws Exception on error accessing storage
     */
    List<T> select(long parent_id) throws Exception;


    /**
     * Set model.* objects to storage
     *
     * @param objects updated object
     * @return true on success
     * @throws Exception on error accessing storage
     */
    int update(List<T> objects) throws Exception;


    /**
     * Create new model.* object in storage
     * {@code id} will be update to new value
     *
     * @param objects new object list
     * @return true on success
     * @throws Exception on error accessing storage
     */
    int insert(List<T> objects) throws Exception;


    /**
     * Delete model.* object from storage by {@code ids}
     *
     * @param ids
     * @return true on success
     * @throws Exception on error accessing storage
     */
    int delete(List<Long> ids) throws Exception;


    /**
     * Delete model.* object from storage by {@code parent_id}
     *
     * @param parent_id
     * @return true on success
     * @throws Exception on error accessing storage
     */
    int delete(long parent_id) throws Exception;

}