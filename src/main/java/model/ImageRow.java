package model;

public class ImageRow {

    // identifier
    public long image_id;

    // filename to save file extension and SEO (not used for now)
    public String filename;

    // altname for <img> tag (not used for now)
    public String altname;

    // sizes to put in tag (not used for now)
    public int width;
    public int height;

    // base64 encoded image to put in src from javascript
    public String bitmap; // it must be BLOB, but for now it's CLOB

}
