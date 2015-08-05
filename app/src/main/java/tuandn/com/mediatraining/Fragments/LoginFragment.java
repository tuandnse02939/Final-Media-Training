package tuandn.com.mediatraining.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Subscription;
import com.google.api.services.youtube.model.SubscriptionListResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import tuandn.com.mediatraining.R;


public class LoginFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    public static final int RC_SIGN_IN = 0;

    private static final String TAG = "MainActivity";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String[] SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE, YouTubeScopes.YOUTUBE_FORCE_SSL, YouTubeScopes.YOUTUBEPARTNER, YouTubeScopes.YOUTUBE_READONLY};
    public static final String API_KEY  = "AIzaSyD0MwUad7hVnPWiuX5HiOWCEnf2VVGd8gY";
    private static final int REQUEST_AUTHORIZATION = 2;
    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    private static final String CLIENT_ID = "310837961882-d59jk6ajrkkor1m742rp3gsm6bse871c.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "xnbT5oEVrnMV9us2Jp3IR2x7";

    private GoogleApiClient mGoogleApiClient;

    private boolean mIntentInProgress;

    private boolean mSignInClicked;

    private ConnectionResult mConnectionResult;

    private SignInButton btnSignIn;

    private Context mContext;
    private Activity mActivity;
    private View view;

    private YouTube mYouTube;
    private String token;
    private GoogleCredential credential;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mContext = getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_login,
                container, false);

        btnSignIn = (SignInButton) view.findViewById(R.id.button_login);


        mGoogleApiClient = new GoogleApiClient.Builder(view.getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(Scopes.PLUS_LOGIN))
                .addScope(new Scope(Scopes.PLUS_ME))
                .build();

        btnSignIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                signInWithGplus();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(view.getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Plus.API)
                    .addScope(new Scope(Scopes.PROFILE))
                    .addScope(new Scope(Scopes.PLUS_LOGIN))
                    .addScope(new Scope(Scopes.PLUS_ME))
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onActivityResult(int requestCode, int responseCode,
                                 Intent intent) {

        if (requestCode == RC_SIGN_IN) {
            if (responseCode != Activity.RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        } else if(requestCode == REQUEST_AUTHORIZATION) {
            setupYoutube();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(),
                    mActivity, 0).show();
            Log.e(TAG, "" + result.getErrorCode());
            return;
        }

        if (!mIntentInProgress) {

            mConnectionResult = result;

            if (mSignInClicked) {

                Log.e(TAG, "" + result.getErrorCode());
                resolveSignInError();
            }
        }

    }

    @Override
    public void onConnected(Bundle arg0) {
        mSignInClicked = false;
        getProfileInformation();
        updateUI(true);
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
        updateUI(false);

    }

    private void updateUI(boolean isSignedIn) {
        if (isSignedIn) {
            btnSignIn.setVisibility(View.GONE);
        } else {
            btnSignIn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sign-in into google
     */
    private void signInWithGplus() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }
    }

    /**
     * Method to resolve any signin errors
     */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(mActivity,
                        RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    /**
     * Fetching user's information name, email, profile pic
     */
    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi
                        .getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String personPhotoUrl = currentPerson.getImage().getUrl();
                final String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                TextView navUsername = (TextView) mActivity.findViewById(R.id.nav_username);
                ImageView navImage = (ImageView) mActivity.findViewById(R.id.nav_image);

                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(PREF_ACCOUNT_NAME, email);
                editor.commit();

                //testttttttttttt

                AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
                    @Override
                    protected String doInBackground(Void... params) {
                        token = null;
                        String full_scope = "oauth2:server:client_id:" + CLIENT_ID +  ":api_scope:" + YouTubeScopes.YOUTUBE + " " + YouTubeScopes.YOUTUBE_READONLY
                                + " " + YouTubeScopes.YOUTUBE_FORCE_SSL + " " + YouTubeScopes.YOUTUBEPARTNER + " " + YouTubeScopes.YOUTUBEPARTNER_CHANNEL_AUDIT;

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


                // end testtttttttttt
                navUsername.setText(personName);
                new DownloadImageTask(navImage)
                        .execute(personPhotoUrl);
                Toast.makeText(mContext,"Hello, " + personName,Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, "Failed. Can't Get User Information",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupYoutube(){
        credential = new GoogleCredential.Builder()
                .setTransport(transport).setJsonFactory(jsonFactory)
                .setClientSecrets(CLIENT_ID, CLIENT_SECRET).setRequestInitializer((new HttpRequestInitializer(){
                    @Override
                    public void initialize(HttpRequest request)
                            throws IOException {
                        request.getHeaders().put("Authorization", "OAuth " + token);
                    }
                })).build();

        mYouTube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), credential)
                .setApplicationName(getActivity().getApplicationContext().getString(R.string.app_name)).build();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                YouTube.Subscriptions.List getListRequest;
                try {
                    getListRequest = mYouTube.subscriptions().list("snippet");
                    getListRequest.setMine(true);
                    getListRequest.setKey(API_KEY);
                    SubscriptionListResponse listResponse = getListRequest.execute();
                    List<Subscription> subscriptions = listResponse.getItems();
                    if(subscriptions == null){
                        Toast.makeText(getActivity().getApplicationContext(),"Nulllllll",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getActivity().getApplicationContext(),"Size: " + subscriptions.size(),Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute((Void) null);
    }

    /**
     * Sign-out from google
     */
    private void signOutFromGplus() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();

            updateUI(false);

        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
