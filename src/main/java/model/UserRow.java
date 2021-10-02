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

    // other fields
    public int tz_utc_offset = -1;

    public void copyTo(UserRow receiver) {
        receiver.user_id = this.user_id;
        receiver.role_id = this.role_id;
        receiver.name = this.name;
        receiver.tz_id = this.tz_id;
        receiver.email = this.email;
        receiver.password = this.password;
        receiver.image_id = this.image_id;
        receiver.owner_id = this.owner_id;
        receiver.tz_utc_offset = this.tz_utc_offset;
    }

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
                ", tz_utc_offset=" + tz_utc_offset +
                '}';
    }
}
