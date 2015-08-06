package tuandn.com.mediatraining.Model;

/**
 * Created by Anh Trung on 8/5/2015.
 */
public class YoutubeVideo {
    String videoID;
    String videoImage;
    String videoName;

    public YoutubeVideo() {
    }

    public YoutubeVideo(String videoID, String videoImage, String videoName) {

        this.videoID = videoID;
        this.videoImage = videoImage;
        this.videoName = videoName;
    }

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public String getVideoImage() {
        return videoImage;
    }

    public void setVideoImage(String videoImage) {
        this.videoImage = videoImage;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }
}
