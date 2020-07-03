package model;

public class ServiceRow {

    // identifier
    public long service_id;

    // title
    public String name;

    // descriptive specification
    public String description;

    // image of service
    public long image_id = -1;

    // length in minutes
    public int duration;

    // cost of service
    public int cost;

    // owner user_id
    public long owner_id;

    // other fields
    public String image_bitmap;
}