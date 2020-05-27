package model;

public class User {

    public long user_id;

    public long role_id;

    public String name;

    public long tz_id;

    public String email;

    public String password;

    public long image_id = -1;

    public long owner_id;

    @Override
    public String toString() {
        return "User{" +
                "user_id=" + user_id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
