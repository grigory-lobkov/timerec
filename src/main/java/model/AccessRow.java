package model;

public class AccessRow {

    // identifier
    public long access_id;

    // linked role
    public long role_id;

    // table name
    public String object_name;

    // access to all objects
    public boolean all_get = false;
    public boolean all_put = false;
    public boolean all_post = false;
    public boolean all_delete = false;

    // access to own objects
    public boolean own_get = false;
    public boolean own_put = false;
    public boolean own_post = false;
    public boolean own_delete = false;

}
