package tuandn.com.mediatraining.Fragments;

import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import tuandn.com.mediatraining.Adapter.ListAudioAdapter;
import tuandn.com.mediatraining.Database.DatabaseHandler;
import tuandn.com.mediatraining.Model.AudioFile;
import tuandn.com.mediatraining.Mp4Wrapper.Mp4ParserWrapper;
import tuandn.com.mediatraining.R;

/**
 * Created by Anh Trung on 7/21/2015.
 */
public class AudioRecordFragment extends Fragment {
    //Record Status
    private static final String NOT_RECORD  = "not_record";
    private static final String RECORDDING  = "recordding";
    private static final String ON_PAUSING  = "on_pausing";
    private static final String FINISHED = "on_stop";

    public static final int DEFAULT_AUDIO_ENCODER =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1
                    ? MediaRecorder.AudioEncoder.AAC
                    : MediaRecorder.AudioEncoder.DEFAULT;

    private Button button1, button2;
    private FloatingActionButton fab,fab_pause,fab_stop,fab_continue;
    private String status, targetFilename;
    private MediaRecorder mediaRecorder;
    private ArrayList<AudioFile> listAudio;

    private boolean isPaused = false;
    private DatabaseHandler handler;
    private String filenameToSaveDB;
    private ListAudioFragment mListFragment = new ListAudioFragment();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_audio_record, container, false);
            return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        button1 = (Button) getView().findViewById(R.id.audio_button1);
        button2 = (Button) getView().findViewById(R.id.audio_button2);

        mListFragment = (ListAudioFragment) getChildFragmentManager().findFragmentById(R.id.list_audio);
        listAudio = new ArrayList<AudioFile>();
        handler = new DatabaseHandler(getActivity().getApplicationContext());

        listAudio = handler.getListAudio();
        if(listAudio.size() != 0){
            ListAudioAdapter la= new ListAudioAdapter(getActivity().getApplicationContext(), listAudio);
            mListFragment.setListAdapter(la);
        }
        else {
            Toast.makeText(getActivity().getApplicationContext(),"Empty",Toast.LENGTH_LONG).show();
        }

        status = NOT_RECORD;
        updateUI();

        handler = new DatabaseHandler(getActivity().getApplicationContext());

        fab = (FloatingActionButton) getView().findViewById (R.id.audio_floating_button);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (status.equals(FINISHED) || status.equals(NOT_RECORD)) {
                    filenameToSaveDB = "Record_"
                            + System.currentTimeMillis()
                            + ".mp3";
                    targetFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                            +filenameToSaveDB ;
                    record();
                    status = RECORDDING;
                    updateUI();
                }
                else {
                    Snackbar.make(getView(),"Recording",Snackbar.LENGTH_LONG).show();
                }
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (status) {
                    case NOT_RECORD:
                        filenameToSaveDB = "Record_"
                                + System.currentTimeMillis()
                                + ".mp3";
                        targetFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                                +filenameToSaveDB ;
                        record();
                        status = RECORDDING;
                        updateUI();
                        break;
                    case RECORDDING:
                        pause();
                        status = ON_PAUSING;
                        updateUI();
                        break;
                    case ON_PAUSING:
                        record();
                        status = RECORDDING;
                        updateUI();
                        break;
                    case FINISHED:
                        filenameToSaveDB = "Record_"
                                + System.currentTimeMillis()
                                + ".mp3";
                        targetFilename = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                                +filenameToSaveDB ;
                        record();
                        status = RECORDDING;
                        updateUI();
                        break;
                }
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (status) {
                    case NOT_RECORD:
                        updateUI();
                        break;
                    case RECORDDING:
                        finish();
                        status = FINISHED;
                        updateUI();
                        break;
                    case ON_PAUSING:
                        finish();
                        status = FINISHED;
                        updateUI();
                        break;
                    case FINISHED:
                        play();
                        updateUI();
                        break;
                }
            }
        });
    }

    private String getTemporaryFileName() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() +File.separator+ "tmprecord";
    }

    private void updateUI(){
        switch (status) {
            case NOT_RECORD:
                button1.setBackgroundResource(R.drawable.record_button);
                button2.setBackgroundResource(R.drawable.play_not_button);
                button2.setEnabled(false);
                break;
            case RECORDDING:
                button1.setBackgroundResource(R.drawable.pause_button);
                button2.setBackgroundResource(R.drawable.stop_button);
                button2.setEnabled(true);
                break;
            case ON_PAUSING:
                button1.setBackgroundResource(R.drawable.record_button);
                button2.setBackgroundResource(R.drawable.stop_button);
                button2.setEnabled(true);
                break;
            case FINISHED:
                button1.setBackgroundResource(R.drawable.record_button);
                button2.setBackgroundResource(R.drawable.play_button);
                button2.setEnabled(true);
                break;
        }
    }

    //Record Audio
    private void record(){
        //Setup Media Recorder
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(DEFAULT_AUDIO_ENCODER);
        mediaRecorder.setOutputFile(getTemporaryFileName());
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Pausing Recording
    private void pause(){
        mediaRecorder.stop();
        mediaRecorder.release();
        isPaused = true;
        Mp4ParserWrapper.append(targetFilename, getTemporaryFileName());
        File f = new File(getTemporaryFileName());
        f.delete();
    }

    //Finish Recording
    private void finish(){
        if (!isPaused) {
            mediaRecorder.stop();
            mediaRecorder.release();
            Mp4ParserWrapper.append(targetFilename, getTemporaryFileName());
        }
        if(handler.addAudio(filenameToSaveDB)){
            Snackbar.make(getView(),"File recorded successfully",Snackbar.LENGTH_LONG).show();
            listAudio = handler.getListAudio();
            ListAudioAdapter la= new ListAudioAdapter(getActivity().getApplicationContext(), listAudio);
            mListFragment.setListAdapter(la);
        }
        else {
            Snackbar.make(getView(),"File recorded fail",Snackbar.LENGTH_LONG).show();
        }
        File f = new File(getTemporaryFileName());
        f.delete();
    }

    //Play Recorded File
    private void play(){
        File file = new File(targetFilename);
        if ( file.exists() ) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "audio/*");
            startActivity(intent);
        }
    }
}
