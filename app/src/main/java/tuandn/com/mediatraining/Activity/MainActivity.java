package tuandn.com.mediatraining.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.api.services.youtube.YouTube;

import java.io.InputStream;

import tuandn.com.mediatraining.Fragments.AudioRecordFragment;
import tuandn.com.mediatraining.Fragments.EmptyFragment;
import tuandn.com.mediatraining.Fragments.ListAudioFragment;
import tuandn.com.mediatraining.Fragments.LoginFragment;
import tuandn.com.mediatraining.Fragments.VideoRecordFragment;
import tuandn.com.mediatraining.Fragments.YoutubeFragment;
import tuandn.com.mediatraining.R;

/**
 * Created by Anh Trung on 7/15/2015.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mNavItemId;
    private static final String NAV_ITEM_ID = "navItemId";
    public GoogleApiClient mGoogleApiClient;
    private LoginFragment mLoginFragment;

    private YoutubeFragment youtubeFragment;
    private AudioRecordFragment audioRecordFragment;
    private VideoRecordFragment videoRecordFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoginFragment = new tuandn.com.mediatraining.Fragments.LoginFragment();
        FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // listen for navigation events
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        // select the correct nav menu item
//        navigationView.getMenu().findItem(mNavItemId).setChecked(true);
        navigationView.getMenu();
        // set up the hamburger icon to open and close the drawer
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.open,
                R.string.close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        //YouTube Fragment
        youtubeFragment= new YoutubeFragment();
        //AudioRecord Fragment
        audioRecordFragment = new AudioRecordFragment();
        //VideoRecordFragment
        videoRecordFragment = new VideoRecordFragment();
        ft2.replace(R.id.fragment_content, videoRecordFragment).commit();

//        getSupportFragmentManager().beginTransaction().add(R.id.list_audio, new ListAudioFragment(), "tag").commit();

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        menuItem.setChecked(true);
        mNavItemId = menuItem.getItemId();

        switch (mNavItemId){
            case R.id.drawer_item_1:
                FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                ft1.replace(R.id.fragment_content,youtubeFragment).commit();
                break;
            case R.id.drawer_item_2:
                FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                ft2.replace(R.id.fragment_content, audioRecordFragment).commit();
                break;
            case R.id.drawer_item_3:
                if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                    ft3.replace(R.id.fragment_content, videoRecordFragment).commit();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Camera doesn't available",Toast.LENGTH_LONG).show();
                }
                break;
//            case R.id.drawer_item_4:
//                FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
//                ft4.replace(R.id.fragment_content, coordinatorFragment).commit();
//                break;
//            case R.id.drawer_item_5:
//                i = new Intent(MainActivity.this,CollapsingToolbarActivity.class);
//                startActivity(i);
//                break;
            default:
                break;
        }
        // allow some time after closing the drawer before performing real navigation
        // so the user can see what is happening
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, mNavItemId);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == mLoginFragment.RC_SIGN_IN) {
            mLoginFragment.onActivityResult(requestCode, resultCode, data);
        } else if(requestCode == VideoRecordFragment.REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK){
            videoRecordFragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
