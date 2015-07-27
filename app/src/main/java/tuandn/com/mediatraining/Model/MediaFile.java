package tuandn.com.mediatraining.Model;

/**
 * Created by Anh Trung on 7/21/2015.
 */
public class MediaFile {
    int id;
    String name;
    String date;
    int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MediaFile() {
    }

    public MediaFile(int id, String name, String date, int type) {

        this.id = id;
        this.name = name;
        this.date = date;
        this.type = type;
    }
}
