package tuandn.com.mediatraining.Model;

/**
 * Created by Anh Trung on 7/24/2015.
 */
public class VideoFile {
    int id;
    String name;
    String date;
    String length;

    public VideoFile() {
    }

    public VideoFile(int id, String name, String date, String length) {

        this.id = id;
        this.name = name;
        this.date = date;
        this.length = length;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }
}
