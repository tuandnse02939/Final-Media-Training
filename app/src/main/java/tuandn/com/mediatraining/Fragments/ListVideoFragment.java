package tuandn.com.mediatraining.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.Scopes;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tuandn.com.mediatraining.Model.YouTubeChannel;
import tuandn.com.mediatraining.R;


/**
 * Created by Anh Trung on 7/8/2015.
 */
public class ListVideoFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    public static final String API_KEY = "AIzaSyD0MwUad7hVnPWiuX5HiOWCEnf2VVGd8gY";
    private static final String PREF_ACCOUNT_NAME = "kingdragon102@gmail.com";
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();


    private ArrayList<YouTubeChannel> channels;
    private GoogleAccountCredential credential;
    private YouTube mYouTube;
    public static final String[] SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE, YouTubeScopes.YOUTUBE_FORCE_SSL};

    public static ListVideoFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ListVideoFragment fragment = new ListVideoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        channels = new ArrayList<YouTubeChannel>();

        SharedPreferences settings = getActivity().getPreferences(Context.MODE_PRIVATE);
        credential = GoogleAccountCredential.usingOAuth2(getActivity().getApplicationContext(),Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        mYouTube = new YouTube.Builder(transport,jsonFactory,credential)
                .setApplicationName("YouTube API Media Training")
                .build();
        YouTube.Subscriptions.List channelRequest;
        try {
            channelRequest = mYouTube.subscriptions().list("snippet");
            channelRequest.setMine(true);
            SubscriptionListResponse listResponse = channelRequest.execute();
            List<Subscription> subscriptions = listResponse.getItems();
            if(subscriptions == null){
                Toast.makeText(getActivity().getApplicationContext(),"Nulllllll",Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getActivity().getApplicationContext(),"Oh YEAH",Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onActivityCreated(savedInstanceState);
    }
}
