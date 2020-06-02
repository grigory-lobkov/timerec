package model;

public class UserRow {

    // identifier
    public long user_id;

    // linked role
    public long role_id;

    // public user name
    public String name;

    // time zone
    public long tz_id;

    // email
    public String email;

    // password
    public String password;

    // avatar
    public long image_id = -1;

    // owner user_id
    public long owner_id;

}
