package model;

public class IntegratorMoodleSessionsRow {

    public long id;
    public long state;
    public String sid;
    public long userid;
    public String sessdata;
    public long timecreated;
    public long timemodified;
    public String firstip;
    public String lastip;

    @Override
    public String toString() {
        return "IntegratorMoodleSessionsRow{" +
                "id=" + id +
                ", state=" + state +
                ", sid='" + sid + '\'' +
                ", userid=" + userid +
                ", sessdata='" + sessdata + '\'' +
                ", timecreated=" + timecreated +
                ", timemodified=" + timemodified +
                ", firstip='" + firstip + '\'' +
                ", lastip='" + lastip + '\'' +
                '}';
    }
}
