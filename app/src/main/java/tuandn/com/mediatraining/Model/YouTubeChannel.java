package tuandn.com.mediatraining.Model;

/**
 * Created by Anh Trung on 7/20/2015.
 */
public class YouTubeChannel {

    String title;
    String image;
    String id;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public YouTubeChannel(String title, String image, String id) {
        this.title = title;
        this.image = image;
        this.id = id;
    }

    public YouTubeChannel(){
    }
}
