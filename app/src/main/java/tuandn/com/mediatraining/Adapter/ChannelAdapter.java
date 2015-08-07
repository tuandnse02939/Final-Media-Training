package tuandn.com.mediatraining.Adapter;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import tuandn.com.mediatraining.Fragments.ListVideoYoutubeFragment;
import tuandn.com.mediatraining.Model.YouTubeChannel;

/**
 * Created by Anh Trung on 7/7/2015.
 */
public class ChannelAdapter extends FragmentPagerAdapter {
    private ArrayList<YouTubeChannel> youTubeChannels;
    private Context context;

    public ChannelAdapter(FragmentManager fm, Context context, ArrayList<YouTubeChannel> channels) {
        super(fm);
        this.context = context;
        this.youTubeChannels = channels;
    }

    @Override
    public int getCount() {
        return youTubeChannels.size();
    }

    @Override
    public Fragment getItem(int position) {
        return ListVideoYoutubeFragment.newInstance(position+1, youTubeChannels.get(position).getId());
    }



    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return youTubeChannels.get(position).getTitle();
    }

}