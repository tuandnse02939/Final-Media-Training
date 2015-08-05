package tuandn.com.mediatraining.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import tuandn.com.mediatraining.Adapter.ChannelAdapter;
import tuandn.com.mediatraining.R;

import static com.google.api.services.youtube.YouTubeScopes.YOUTUBEPARTNER;

/**
 * Created by Anh Trung on 7/17/2015.
 */
public class YoutubeFragment extends Fragment{

    private static final String TAG = "MainActivity";
    public static final String[] SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE, YouTubeScopes.YOUTUBE_FORCE_SSL, YOUTUBEPARTNER, YouTubeScopes.YOUTUBE_READONLY};
    public static final String API_KEY  = "AIzaSyD0MwUad7hVnPWiuX5HiOWCEnf2VVGd8gY";
    public static final int REQUEST_AUTHORIZATION = 2;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private static final String CLIENT_ID_WEB       = "310837961882-d59jk6ajrkkor1m742rp3gsm6bse871c.apps.googleusercontent.com";
    private YouTube mYouTube;
    private String token;
    private GoogleAccountCredential credential;
    private Context mContext;
    private Activity mActivity;
    private String channelTitles[];
    private String email;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_youtube, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mActivity = getActivity();
        mContext  = mActivity.getApplicationContext();

        tabLayout = (TabLayout) getView().findViewById(R.id.tabs);
        viewPager = (ViewPager) getView().findViewById(R.id.viewpager);

        getChannelInfo();

        super.onActivityCreated(savedInstanceState);
    }

    private void getChannelInfo(){
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                token = null;
                String full_scope = "oauth2:server:client_id:" + CLIENT_ID_WEB +  ":api_scope:" + YouTubeScopes.YOUTUBE + " " + YouTubeScopes.YOUTUBE_READONLY
                        + " " + YouTubeScopes.YOUTUBE_FORCE_SSL + " " + YOUTUBEPARTNER;
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                email = sharedPref.getString(LoginFragment.PREF_ACCOUNT_NAME,"accountname");
                try {
                    token = GoogleAuthUtil.getToken(
                            mContext,
                            email,
                            full_scope);
                } catch (IOException transientEx) {
                    // Network or server error, try later
                    Log.e(TAG, transientEx.toString());
                } catch (UserRecoverableAuthException e) {
                    // Recover (with e.getIntent())
                    Log.e(TAG, e.toString());
                    Intent recover = e.getIntent();
                    mActivity.startActivityForResult(recover, REQUEST_AUTHORIZATION);
                } catch (GoogleAuthException authEx) {
                    Log.e(TAG, authEx.toString());
                }
                if(token != null){
                    setupYoutube();
                }
                return token;
            }

            @Override
            protected void onPostExecute(final String mToken) {
                Log.i(TAG, "Access token retrieved:" + token);
            }

        };
        task.execute();
    }

    private void setupYoutube(){

        credential = GoogleAccountCredential.usingOAuth2(mContext, Arrays.asList(SCOPES));
        SharedPreferences settings = mActivity.getPreferences(Context.MODE_PRIVATE);
        credential.setSelectedAccountName(email);
        // YouTube client
        mYouTube = new YouTube.Builder(transport, jsonFactory, credential)
                .setApplicationName(getActivity().getApplicationContext().getString(R.string.app_name)).build();

        AsyncTask<Void, Void, String[]> task2 = new AsyncTask<Void, Void, String[]>() {
            @Override
            protected String[] doInBackground(Void... voids) {
                YouTube.Subscriptions.List getListRequest;
                try {
                    getListRequest = mYouTube.subscriptions().list("snippet");
                    getListRequest.setMine(true);
                    getListRequest.setKey(API_KEY);
                    SubscriptionListResponse listResponse = getListRequest.execute();
                    List<Subscription> subscriptions = listResponse.getItems();
                    if(subscriptions.size() == 0){
                        Toast.makeText(getActivity().getApplicationContext(), "Nulllllll", Toast.LENGTH_LONG).show();
                    }
                    else{
                        channelTitles = new String[subscriptions.size()];
                        for(int i=0; i < subscriptions.size(); i++){
                            String title = subscriptions.get(i).getSnippet().getTitle();
                            channelTitles[i] = title;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return channelTitles;
            }

            @Override
            protected void onPostExecute(String[] channelList) {
                viewPager.setAdapter(new ChannelAdapter(getActivity().getSupportFragmentManager(),
                        getActivity(),channelList));
                tabLayout.setupWithViewPager(viewPager);
            }

        }.execute((Void) null);
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode, Intent intent) {

        if(requestCode == REQUEST_AUTHORIZATION) {
            setupYoutube();
        }
    }

}
