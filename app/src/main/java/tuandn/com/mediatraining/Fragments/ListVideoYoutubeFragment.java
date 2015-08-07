package tuandn.com.mediatraining.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.Scopes;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tuandn.com.mediatraining.Adapter.ListVideoYoutubeAdapter;
import tuandn.com.mediatraining.Model.YoutubeVideo;
import tuandn.com.mediatraining.R;


/**
 * Created by Anh Trung on 7/8/2015.
 */
public class ListVideoYoutubeFragment extends Fragment {
    private static final String TAG = "MainActivity";
    public static final String ARG_PAGE = "ARG_PAGE";
    public static final String CHANNEL_ID = "CHANNEL_ID";
    public static final String API_KEY  = "AIzaSyD0MwUad7hVnPWiuX5HiOWCEnf2VVGd8gY";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private YouTube.Channels.List query;
    private String token;
    private Context mContext;
    private Activity mActivity;
    private AsyncTask<String, Void, ArrayList<YoutubeVideo>> getListVideoID;
    private String channelID;

    private ArrayList<YoutubeVideo> listVideoID;
    private GoogleAccountCredential credential;
    private YouTube mYouTube;
    public static final String[] SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE, YouTubeScopes.YOUTUBE_FORCE_SSL, YouTubeScopes.YOUTUBEPARTNER, YouTubeScopes.YOUTUBE_READONLY};
    private List2Fragment mListFragment;

    public static ListVideoYoutubeFragment newInstance(int page, String listID) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putString(CHANNEL_ID, listID);
        ListVideoYoutubeFragment fragment = new ListVideoYoutubeFragment();
        fragment.setArguments(args);
        fragment.setRetainInstance(true);
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

        mActivity = getActivity();
        mContext  = mActivity.getApplicationContext();

        mListFragment = (List2Fragment) getChildFragmentManager().findFragmentById(R.id.list_video_youtube2);
        setupYoutube();
        Bundle b = getArguments();
        channelID = b.getString(CHANNEL_ID);
        super.onActivityCreated(savedInstanceState);
    }

    private void setupYoutube(){

        credential = GoogleAccountCredential.usingOAuth2(mContext, Arrays.asList(SCOPES));
        SharedPreferences settings = mActivity.getPreferences(Context.MODE_PRIVATE);
        credential.setSelectedAccountName(settings.getString(LoginFragment.PREF_ACCOUNT_NAME,null));
        // YouTube client
        mYouTube = new YouTube.Builder(transport, jsonFactory, credential)
                .setApplicationName(getActivity().getApplicationContext().getString(R.string.app_name)).build();

        AsyncTask<Void, Void, String> task2 = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                YouTube.Channels.List getListRequest;
                String listVideoId = null;
                try {
                    getListRequest = mYouTube.channels().list("contentDetails");
                    getListRequest.setId(channelID);
                    getListRequest.setKey(API_KEY);
                    getListRequest.setFields("items/contentDetails,nextPageToken,pageInfo");
                    ChannelListResponse listResponse = getListRequest.execute();
                    List<Channel> channel = listResponse.getItems();
                    if(channel.size() == 0){
                        System.out.println("Cannot get Playlist ID");
                    }
                    else{
                        listVideoId = channel.get(0).getContentDetails().getRelatedPlaylists().getUploads();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return listVideoId;
            }

            @Override
            protected void onPostExecute(String listID) {
                getListVideoID.execute(listID);
            }

        }.execute((Void) null);

         getListVideoID = new AsyncTask<String, Void, ArrayList<YoutubeVideo>>() {
             @Override
            protected ArrayList<YoutubeVideo> doInBackground(String... params) {
                listVideoID = new ArrayList<YoutubeVideo>();
                try {

                    // Define a list to store items in the list of uploaded videos.
                    List<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

                    // Retrieve the playlist of the channel's uploaded videos.
                    YouTube.PlaylistItems.List playlistItemRequest =
                            mYouTube.playlistItems().list("id,contentDetails,snippet");
                    playlistItemRequest.setPlaylistId(params[0]);
                    playlistItemRequest.setFields(
                            "items(contentDetails/videoId,snippet/title,snippet/thumbnails),nextPageToken,pageInfo");
                    playlistItemRequest.setKey(API_KEY);
                    String nextToken = "";

                    do {
                        playlistItemRequest.setPageToken(nextToken);
                        PlaylistItemListResponse playlistItemResult = playlistItemRequest.execute();

                        playlistItemList.addAll(playlistItemResult.getItems());

                        nextToken = playlistItemResult.getNextPageToken();
                    } while (nextToken != null && playlistItemList.size()<=30);

                    if(playlistItemList.size() == 0){
                        System.out.println("Cannot get List Video ID");
                    }
                    else{
                        listVideoID = new ArrayList<YoutubeVideo>();

                        for(PlaylistItem item : playlistItemList){
                            YoutubeVideo youtubeVideo = new YoutubeVideo();
                            youtubeVideo.setVideoID(item.getContentDetails().getVideoId());
                            youtubeVideo.setVideoImage(item.getSnippet().getThumbnails().getMedium().getUrl());
                            youtubeVideo.setVideoName(item.getSnippet().getTitle());
                            listVideoID.add(youtubeVideo);
                        }
                        return listVideoID;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(ArrayList<YoutubeVideo> listVideo) {
                if(listVideo.size() != 0){
                    ListVideoYoutubeAdapter la= new ListVideoYoutubeAdapter(getActivity().getApplicationContext(), listVideo);
                    mListFragment.setListAdapter(la);
                }
                else {
                    mListFragment.setEmptyText(getString(R.string.no_file_recorded));
                    if (isResumed())
                        mListFragment.setListShown(true);
                    else
                        mListFragment.setListShownNoAnimation(true);
                }
            }

        };
    }

}
