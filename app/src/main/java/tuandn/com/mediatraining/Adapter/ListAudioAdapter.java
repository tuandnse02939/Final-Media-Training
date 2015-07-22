package tuandn.com.mediatraining.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tuandn.com.mediatraining.Model.AudioFile;
import tuandn.com.mediatraining.R;

/**
 * Created by Anh Trung on 7/22/2015.
 */
public class ListAudioAdapter extends ArrayAdapter<AudioFile> {

    private Context context;
    private final ArrayList<AudioFile> mList;

    public ListAudioAdapter(Context context, ArrayList<AudioFile> mList) {
        super(context, R.layout.fragment_list_audio, mList);
        this.context = context;
        this.mList = mList;
    }

    static class ViewHolder {
        TextView name;
        TextView date;
        Button play_audio;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewHolder holder;
        if(convertView == null){
            convertView = inflater.from(context).inflate(R.layout.fragment_list_audio,null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.audio_file_name);
            holder.date = (TextView) convertView.findViewById(R.id.audio_file_date);
            holder.play_audio = (Button) convertView.findViewById(R.id.audio_play_button);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        //Set Audio Detail
        holder.name.setText(mList.get(position).getName());
        holder.date.setText(mList.get(position).getDate());

        return convertView;
    }

}
