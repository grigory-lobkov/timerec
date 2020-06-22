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

    @Override
    public String toString() {
        return "UserRow{" +
                "user_id=" + user_id +
                ", role_id=" + role_id +
                ", name='" + name + '\'' +
                ", tz_id=" + tz_id +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", image_id=" + image_id +
                ", owner_id=" + owner_id +
                '}';
    }
}
