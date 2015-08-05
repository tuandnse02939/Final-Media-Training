package tuandn.com.mediatraining.Adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import tuandn.com.mediatraining.Fragments.ListVideoYoutubeFragment;

/**
 * Created by Anh Trung on 7/7/2015.
 */
public class ChannelAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 3;
    private String tabTitles[];
    private Context context;

    public ChannelAdapter(FragmentManager fm, Context context, String[] channel) {
        super(fm);
        this.context = context;
        this.tabTitles = channel;
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public Fragment getItem(int position) {
        return ListVideoYoutubeFragment.newInstance(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}