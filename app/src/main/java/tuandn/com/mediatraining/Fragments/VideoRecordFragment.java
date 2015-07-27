package tuandn.com.mediatraining.Fragments;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

import tuandn.com.mediatraining.Activity.MainActivity;
import tuandn.com.mediatraining.R;

/**
 * Created by Anh Trung on 7/24/2015.
 */
public class VideoRecordFragment extends Fragment{

    private FloatingActionButton fab;
    public static final int REQUEST_VIDEO_CAPTURE = 1;
    private Activity mActivity;
    private Context mContext;
    private VideoView mVideoView;

    private String filenameToSaveDB;
    private String targetFilename;
    private Button videoRecord;
    private MediaRecorder recorder;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_record, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mActivity = getActivity();
        mContext = getActivity().getApplicationContext();
        mVideoView = (VideoView) getView().findViewById(R.id.video_view);
        videoRecord = (Button) getView().findViewById(R.id.video_record);
        surfaceView = (SurfaceView) getView().findViewById(R.id.surface);
        surfaceHolder = surfaceView.getHolder();

        //Setting for Floating Button
        fab = (FloatingActionButton) getView().findViewById (R.id.video_floating_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    useCameraAPI();
                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    useCameraAPI();
                }
            }
        });

        videoRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recorder.stop();
                recorder.reset();
                recorder.release();

                camera.stopPreview();
                camera.release();
                Toast.makeText(mContext,"Stopped",Toast.LENGTH_LONG).show();
            }
        });
    }




    private void useCameraAPI(){
        camera = Camera.open();
        // If necessary, modify the returned Camera.Parameters object and call setParameters(Camera.Parameters).
        camera.getParameters();
        // Set Camera orientation
        camera.setDisplayOrientation(90);
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.stopPreview();
            camera.unlock();

            recorder = new MediaRecorder();
            recorder.setCamera(camera);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            filenameToSaveDB = "Video_"
                    + System.currentTimeMillis()
                    + ".mp4";
            targetFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                    +filenameToSaveDB;
            recorder.setOutputFile(targetFilename);
            recorder.prepare();
            recorder.start();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void useCamera2API(){

    }
}
