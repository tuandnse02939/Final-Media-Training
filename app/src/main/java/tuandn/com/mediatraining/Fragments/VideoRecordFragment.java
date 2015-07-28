package tuandn.com.mediatraining.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import tuandn.com.mediatraining.Adapter.ListVideoAdapter;
import tuandn.com.mediatraining.Database.DatabaseHandler;
import tuandn.com.mediatraining.Model.MediaFile;
import tuandn.com.mediatraining.Mp4Wrapper.Mp4ParserWrapper;
import tuandn.com.mediatraining.R;

/**
 * Created by Anh Trung on 7/24/2015.
 */
public class VideoRecordFragment extends Fragment{

    public static final String RECORDING = "RECORDING";
    public static final String ON_PAUSING = "ON_PAUSING";

    private FloatingActionButton fab;
    public static final int     REQUEST_VIDEO_CAPTURE = 1;
    private Activity            mActivity;
    private Context             mContext;
    private VideoView           mVideoView;

    private String              filenameToSaveDB;
    private String              targetFilename;
    private String              status;
    private Button              videoRecord1,videoRecord2, changeCamera, flashControl;
    private MediaRecorder       recorder;
    private SurfaceView         surfaceView;
    private SurfaceHolder       surfaceHolder;
    private Camera              camera;
    private android.support.design.widget.CoordinatorLayout   mainLayout;
    private RelativeLayout      secondLayout;
    private DatabaseHandler     handler;
    private ArrayList<MediaFile> listVideo;
    private boolean             cameraConfigured = false;
    private int                 cameraID = 0;
    private boolean             inPreview = false;
    private boolean             isFlashAvailable = true;
    private boolean             isFlashOn = false;
    private Camera.Parameters   parameters;

    private ListVideoRecordedFragment mListFragment = new ListVideoRecordedFragment();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_record, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity       = getActivity();
        mContext        = getActivity().getApplicationContext();

        mListFragment = (ListVideoRecordedFragment) getChildFragmentManager().findFragmentById(R.id.video_file_list);

        handler = new DatabaseHandler(mContext);

        mainLayout   = (CoordinatorLayout) getView().findViewById(R.id.main_video_record);
        secondLayout = (RelativeLayout) getView().findViewById(R.id.second_video_record);

        //Update UI Layout
        mainLayout.setVisibility(View.VISIBLE);
        secondLayout.setVisibility(View.GONE);

        mVideoView      = (VideoView)   getView().findViewById(R.id.video_view);
        mVideoView.setVisibility(View.VISIBLE);
        videoRecord1 = (Button)      getView().findViewById(R.id.video_record1);
        videoRecord2 = (Button)      getView().findViewById(R.id.video_record2);
        changeCamera = (Button)      getView().findViewById(R.id.video_record_change_camera);
        flashControl = (Button)      getView().findViewById(R.id.flash);
        surfaceView     = (SurfaceView) getView().findViewById(R.id.surface);
        surfaceHolder   = surfaceView.getHolder();

        reloadList();

        if(!getActivity().getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){
            isFlashAvailable = false;
        }

        //Setting for Floating Button
        fab = (FloatingActionButton) getView().findViewById (R.id.video_floating_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update UI Layout
                inPreview = true;
                mVideoView.setVisibility(View.GONE);
                mainLayout.setVisibility(View.GONE);
                secondLayout.setVisibility(View.VISIBLE);
                status = RECORDING;
                filenameToSaveDB = "Video_"
                        + System.currentTimeMillis()
                        + ".mp4";
                targetFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                        +filenameToSaveDB;
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    useCameraAPI();
                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    useCameraAPI();
                }
            }
        });

        videoRecord1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status != ON_PAUSING) {
                    recorder.stop();
                    recorder.reset();
                    recorder.release();
                    Mp4ParserWrapper.append(targetFilename, getTemporaryFileName());
                    File f = new File(getTemporaryFileName());
                    f.delete();
                    camera.stopPreview();
                    camera.release();
                }
                mVideoView.setVisibility(View.VISIBLE);
                if (handler.addMediaFile(filenameToSaveDB, handler.VIDEO_TYPE)) {
                    Toast.makeText(mContext, mActivity.getString(R.string.video_record_successful), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, mActivity.getString(R.string.video_record_failed), Toast.LENGTH_LONG).show();
                }
                mainLayout.setVisibility(View.VISIBLE);
                secondLayout.setVisibility(View.GONE);
                reloadList();
                inPreview = false;
            }
        });

        videoRecord2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status == RECORDING) {
                    pause();
                    status = ON_PAUSING;
                    videoRecord2.setBackgroundResource(R.drawable.video_camera_icon);
                }
                else if(status == ON_PAUSING){
                    status = RECORDING;
                    videoRecord2.setBackgroundResource(R.drawable.pause_button);
                    useCameraAPI();
                }
            }
        });

        changeCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inPreview) {
                    camera.stopPreview();
                    recorder.stop();
                    recorder.reset();
                    recorder.release();
                    File f = new File(targetFilename);
                    f.delete();
                }
                camera.release();
                cameraID = (cameraID + 1) % 2;
                useCameraAPI();
            }
        });

        flashControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFlashAvailable){
                    flashControl.setBackgroundResource(R.drawable.flash_off_icon);
                }
                else {
                    camera.stopPreview();
                    if(isFlashOn) {
                        flashControl.setBackgroundResource(R.drawable.flash_off_icon);
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        camera.setParameters(parameters);
                        camera.startPreview();
                        isFlashOn = false;
                    }
                    else{
                        flashControl.setBackgroundResource(R.drawable.flash_on);
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        camera.setParameters(parameters);
                        camera.startPreview();
                        isFlashOn = true;
                    }
                }
            }
        });
    }

    private void pause() {
        recorder.stop();
        recorder.reset();
        recorder.release();

        camera.stopPreview();
        camera.release();
        Toast.makeText(mContext, "Pause", Toast.LENGTH_LONG).show();

        Mp4ParserWrapper.append(targetFilename, getTemporaryFileName());
        File f = new File(getTemporaryFileName());
        f.delete();
    }

    private void reloadList(){
        listVideo = handler.getListVideo();
        //Set List for ListVideoRecoredFragment
        if(listVideo.size() != 0){
            ListVideoAdapter la= new ListVideoAdapter(getActivity(), listVideo);
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


    private void useCameraAPI(){
        try {
            setupRecorder();
            recorder.setOutputFile(getTemporaryFileName());
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void useCamera2API(){

    }

    private void setupRecorder(){
        try {
            camera = Camera.open(cameraID);
            camera.setDisplayOrientation(90);
            initPreview(1024, 720);
            camera.setPreviewDisplay(surfaceHolder);
            camera.stopPreview();
            camera.unlock();
            // If necessary, modify the returned Camera.Parameters object and call setParameters(Camera.Parameters).
            recorder = new MediaRecorder();
            recorder.setCamera(camera);
//            recorder.setVideoSize(1024,720);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            System.out.print("Size: " + size.width + ", " + size.height);
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }

        return (result);
    }

    private void initPreview(int width, int height) {
        if (camera != null && surfaceHolder.getSurface() != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
            } catch (Throwable t) {
                Toast.makeText(mActivity, t.getMessage(), Toast.LENGTH_LONG).show();
            }

            if (!cameraConfigured) {
                parameters = camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height, parameters);

                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    camera.setParameters(parameters);
                    cameraConfigured = true;
                }
            }
        }
    }

    private String getTemporaryFileName() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() +File.separator+ "tmpvideo";
    }
}
