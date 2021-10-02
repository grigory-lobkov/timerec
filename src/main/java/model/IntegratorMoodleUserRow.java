package model;

public class IntegratorMoodleUserRow {

    public long id;
    public int confirmed;
    public int deleted;
    public int suspended;
    public long mnethostid;
    public String username;
    public String firstname;
    public String lastname;
    public String email;
    public int emailstop;
    public String lang;
    public String timezone;
    public long firstaccess;
    public long lastaccess;
    public long lastlogin;
    public long currentlogin;
    public String lastip;
    public long timecreated;
    public long timemodified;

    @Override
    public String toString() {
        return "IntegratorMoodleUserRow{" +
                "id=" + id +
                ", confirmed=" + confirmed +
                ", deleted=" + deleted +
                ", suspended=" + suspended +
                ", mnethostid=" + mnethostid +
                ", username='" + username + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", emailstop=" + emailstop +
                ", lang='" + lang + '\'' +
                ", timezone='" + timezone + '\'' +
                ", firstaccess=" + firstaccess +
                ", lastaccess=" + lastaccess +
                ", lastlogin=" + lastlogin +
                ", currentlogin=" + currentlogin +
                ", lastip='" + lastip + '\'' +
                ", timecreated=" + timecreated +
                ", timemodified=" + timemodified +
                '}';
    }
}
