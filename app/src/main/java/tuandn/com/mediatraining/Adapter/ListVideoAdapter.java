package tuandn.com.mediatraining.Adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;

import tuandn.com.mediatraining.Model.MediaFile;
import tuandn.com.mediatraining.R;

/**
 * Created by Anh Trung on 7/24/2015.
 */
public class ListVideoAdapter extends ArrayAdapter<MediaFile> {
    private Activity activity;
    private final ArrayList<MediaFile> mList;
    private VideoView videoView;

    public ListVideoAdapter(Activity activity, ArrayList<MediaFile> mList) {
        super(activity, R.layout.fragment_list_audio, mList);
        this.activity = activity;
        this.mList = mList;
    }

    static class ViewHolder {
        TextView name;
        TextView date;
        TextView length;
        Button play_video;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewHolder holder;
        videoView = (VideoView) activity.findViewById(R.id.video_view);
        if(convertView == null){
            convertView = inflater.from(activity).inflate(R.layout.fragment_list_video,null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.video_file_name);
            holder.date = (TextView) convertView.findViewById(R.id.video_file_date);
            holder.length = (TextView) convertView.findViewById(R.id.video_file_length);
            holder.play_video = (Button) convertView.findViewById(R.id.video_play_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        //Set Audio Detail
        holder.name.setText(mList.get(position).getName());
        final String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + mList.get(position).getName();
        holder.date.setText(mList.get(position).getDate());
        holder.play_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(fileName);
                if (file.exists()) {
                    MediaController videoMediaController = new MediaController(activity);
                    videoView.setVideoPath(fileName);
                    videoMediaController.setMediaPlayer(videoView);
                    videoView.setMediaController(videoMediaController);
                    videoView.requestFocus();
                    videoView.start();
                } else {
                    Snackbar.make(v, "File not found", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        return convertView;
    }
}
