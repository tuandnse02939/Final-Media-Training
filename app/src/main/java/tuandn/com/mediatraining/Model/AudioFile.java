package tuandn.com.mediatraining.Model;

/**
 * Created by Anh Trung on 7/21/2015.
 */
public class AudioFile {
    int id;
    String name;
    String date;

    public AudioFile(int id, String name, String date) {
        this.id = id;
        this.name = name;
        this.date = date;
    }

    public AudioFile(){

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
}
