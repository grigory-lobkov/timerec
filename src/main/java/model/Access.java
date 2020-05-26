package model;

public class Access {

    public long access_id;

    public long role_id;

    public String object_name;

    public boolean all_get = false;
    public boolean all_put = false;
    public boolean all_post = false;
    public boolean all_delete = false;

    public boolean own_get = false;
    public boolean own_put = false;
    public boolean own_post = false;
    public boolean own_delete = false;

}
