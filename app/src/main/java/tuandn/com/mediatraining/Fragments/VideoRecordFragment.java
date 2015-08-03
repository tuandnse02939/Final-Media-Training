package tuandn.com.mediatraining.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;

import tuandn.com.mediatraining.Adapter.ListVideoAdapter;
import tuandn.com.mediatraining.Database.DatabaseHandler;
import tuandn.com.mediatraining.Model.MediaFile;
import tuandn.com.mediatraining.Mp4Wrapper.Mp4ParserWrapper;
import tuandn.com.mediatraining.R;

/**
 * Created by Anh Trung on 7/24/2015.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class VideoRecordFragment extends Fragment{

    public static final String RECORDING = "RECORDING";
    public static final String ON_PAUSING = "ON_PAUSING";

    public static final String HD1080 = "HD1080p";
    public static final String HD720 = "HD720p";
    public static final String HQ480 = "HQ480p";
    public static final String QVGA = "240p";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


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
    private Spinner             videoSpinner;
    private CamcorderProfile    profile;
    private int                 width = 0,height = 0;
    private boolean             isCameraAvailable = true;
    private boolean             isSetCamera = false;
    private boolean             isFirstTimeCallRecord = true;
    private boolean             isChangingFlash = false;

    private ListVideoRecordedFragment mListFragment = new ListVideoRecordedFragment();

    // Camera2
    /**
     * A refernce to the opened {@link android.hardware.camera2.CameraDevice}.
     */
    private CameraDevice mCameraDevice;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * A reference to the current {@link android.hardware.camera2.CameraCaptureSession} for preview.
     */
    private CameraCaptureSession mPreviewSession;
    /**
     * Camera preview.
     */
    private CaptureRequest.Builder mPreviewBuilder;
    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    private TextureView mTextureView;
    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * The {@link android.util.Size} of video recording.
     */
    private Size mVideoSize;


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

        mTextureView = (TextureView) getView().findViewById(R.id.texture);

        //Spinner
        videoSpinner = (Spinner) getView().findViewById(R.id.spinner_video_size);

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
                    useCamera2API();
                }
                if(!isFirstTimeCallRecord){
                    setupRecorder();
                }
                isFirstTimeCallRecord = false;
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
                    recorder.stop();
                    recorder.reset();
                    recorder.release();

                    camera.stopPreview();
                    camera.release();

                    Mp4ParserWrapper.append(targetFilename, getTemporaryFileName());
                    File f = new File(getTemporaryFileName());
                    f.delete();

                    isChangingFlash = true;
                    setupCamera();
                    setupRecorder();
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
        surfaceView.setVisibility(View.VISIBLE);
        mTextureView.setVisibility(View.GONE);
        setupCamera();
    }

    private void useCamera2API(){
        surfaceView.setVisibility(View.GONE);
        mTextureView.setVisibility(View.VISIBLE);
        setupCamera2();
    }

    private void setupCamera(){
        try {
            camera = Camera.open(cameraID);
            camera.setDisplayOrientation(90);
            initPreview(1024, 768);
            camera.setPreviewDisplay(surfaceHolder);
            if (!isSetCamera){
                setVideoSizeMain();
            }
            if (isChangingFlash){
                if(isFlashOn) {
                    flashControl.setBackgroundResource(R.drawable.flash_off_icon);
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                    isFlashOn = false;
                }
                else{
                    flashControl.setBackgroundResource(R.drawable.flash_on);
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameters);
                    isFlashOn = true;
                }
                isChangingFlash = false;
            } else {
                flashControl.setBackgroundResource(R.drawable.flash_off_icon);
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
            }
            camera.unlock();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupRecorder(){
        recorder = new MediaRecorder();
        recorder.setCamera(camera);
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //Check if user haven't choose Video Size
        if(height == 0) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        }
        width   = profile.videoFrameWidth;
        height  = profile.videoFrameHeight;
        recorder.setVideoSize(width, height);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(getTemporaryFileName());
        try {
            recorder.prepare();
            recorder.start();
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

    //setVideoSizeSpinner
    private void setVideoSizeMain(){
        ArrayList<String> spinnerArray = new ArrayList<String>();
        List<Camera.Size> list = camera.getParameters().getSupportedPictureSizes();
        for (Camera.Size size:list){
            if(size.height == CamcorderProfile.get(CamcorderProfile.QUALITY_1080P).videoFrameHeight
                    & size.width == CamcorderProfile.get(CamcorderProfile.QUALITY_1080P).videoFrameWidth){
                spinnerArray.add(HD1080);
            }
            else if(size.height == CamcorderProfile.get(CamcorderProfile.QUALITY_720P).videoFrameHeight
                    & size.width == CamcorderProfile.get(CamcorderProfile.QUALITY_720P).videoFrameWidth){
                spinnerArray.add(HD720);
                }
            else if(size.height == CamcorderProfile.get(CamcorderProfile.QUALITY_480P).videoFrameHeight
                    & size.width == CamcorderProfile.get(CamcorderProfile.QUALITY_480P).videoFrameWidth){
                spinnerArray.add(HQ480);
            }
            else if(size.height == CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA).videoFrameHeight
                    & size.width == CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA).videoFrameWidth){
                spinnerArray.add(QVGA);
            }
            System.out.println("XXYY: " + size.height+ " " + size.width);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.spinner_item, spinnerArray);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        videoSpinner.setAdapter(adapter);

        videoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equals(HD1080)) {
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P);
                } else if (parent.getItemAtPosition(position).toString().equals(HD720)) {
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
                } else if (parent.getItemAtPosition(position).toString().equals(HQ480)) {
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                } else if (parent.getItemAtPosition(position).toString().equals(QVGA)) {
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA);
                }
                if(!isCameraAvailable){
                    recorder.stop();
                    recorder.reset();
                    recorder.release();
                }
                if(isSetCamera) {
                    camera.stopPreview();
                    File f = new File(targetFilename);
                    f.delete();
                    camera.release();
                    setupCamera();
                }
                isSetCamera = true;
                isCameraAvailable = false;
                setupRecorder();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    //------------Camera2-----------------------

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupCamera2(){
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraIdStr = cameraManager.getCameraIdList()[0];
            CameraCharacteristics cc = cameraManager.getCameraCharacteristics(cameraIdStr);
            StreamConfigurationMap streamConfigs = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mVideoSize = chooseVideoSize(streamConfigs.getOutputSizes(MediaRecorder.class));
            mPreviewSize = chooseOptimalSize(streamConfigs.getOutputSizes(SurfaceTexture.class),
                    width, height, mVideoSize);

            cameraManager.openCamera(cameraIdStr, mStateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            setUpMediaRecorder2();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<Surface>();

            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            Surface recorderSurface = recorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Activity activity = getActivity();
                    if (null != activity) {
                        Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpMediaRecorder2() throws IOException {
        final Activity activity = getActivity();
        if (null == activity) {
            return;
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setVideoEncodingBitRate(10000000);
        recorder.setVideoFrameRate(30);
        if(height == 0) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        }
        width   = profile.videoFrameWidth;
        height  = profile.videoFrameHeight;
        recorder.setVideoSize(width, height);
        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int orientation = ORIENTATIONS.get(rotation);
        recorder.setOrientationHint(orientation);
        recorder.setOutputFile(getTemporaryFileName());
        recorder.prepare();
    }

    /**
     * Update the camera preview. {@link #startPreview()} needs to be called in advance.
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void closeCamera(){
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (null != recorder) {
            recorder.release();
            recorder = null;
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
