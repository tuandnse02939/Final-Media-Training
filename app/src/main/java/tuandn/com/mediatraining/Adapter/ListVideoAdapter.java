package tuandn.com.mediatraining.Adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import tuandn.com.mediatraining.Model.VideoFile;

/**
 * Created by Anh Trung on 7/24/2015.
 */
public class ListVideoAdapter extends ArrayAdapter<VideoFile> {
    public ListVideoAdapter(Context context, int resource) {
        super(context, resource);
    }
}
