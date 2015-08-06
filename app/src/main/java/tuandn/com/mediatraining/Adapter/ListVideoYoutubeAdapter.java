package tuandn.com.mediatraining.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.imageloader.ImageLoader;

import java.util.ArrayList;

import tuandn.com.mediatraining.Activity.MainActivity;
import tuandn.com.mediatraining.Activity.PlayerViewDemoActivity;
import tuandn.com.mediatraining.Model.YoutubeVideo;
import tuandn.com.mediatraining.R;

/**
 * Created by Anh Trung on 8/5/2015.
 */
public class ListVideoYoutubeAdapter extends ArrayAdapter<YoutubeVideo>{

    private Context context;
    private ArrayList<YoutubeVideo> mList;
    private ImageLoader imageLoader;

    public ListVideoYoutubeAdapter(Context context, ArrayList<YoutubeVideo> mList) {
        super(context, R.layout.fragment_list_youtube_video, mList);
        this.context = context;
        this.mList = mList;
    }

    static class ViewHolder {
        ImageView image;
        TextView name;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewHolder holder;
        if(convertView == null){
            convertView = inflater.from(context).inflate(R.layout.fragment_list_youtube_video,null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.youtube_video_name);
            holder.image = (ImageView) convertView.findViewById(R.id.youtube_video_image);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        //Set Audio Detail
        holder.name.setText(mList.get(position).getVideoName());

        //Set Friend Image
        ImageLoader.Callback callback = new ImageLoader.Callback() {
            @Override
            public void onImageLoaded(ImageView imageView, String s) {
                holder.image = imageView;
            }

            @Override
            public void onImageError(ImageView imageView, String s, Throwable throwable) {

            }
        };

        imageLoader = new ImageLoader();
        imageLoader.bind(holder.image, mList.get(position).getVideoImage(), callback);


        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PlayerViewDemoActivity.class);
                intent.putExtra("videoCode", mList.get(position).getVideoID());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        return convertView;
    }
}
