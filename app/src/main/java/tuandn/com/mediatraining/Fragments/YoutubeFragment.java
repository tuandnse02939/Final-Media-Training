package tuandn.com.mediatraining.Fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.api.services.youtube.YouTube;

import tuandn.com.mediatraining.Adapter.ChannelAdapter;
import tuandn.com.mediatraining.R;

/**
 * Created by Anh Trung on 7/17/2015.
 */
public class YoutubeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_youtube, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        TabLayout tabLayout = (TabLayout) getView().findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) getView().findViewById(R.id.viewpager);
        viewPager.setAdapter(new ChannelAdapter(getActivity().getSupportFragmentManager(),
                getActivity()));
        tabLayout.setupWithViewPager(viewPager);
        super.onActivityCreated(savedInstanceState);

    }

}
