package com.example.smarija.mediaplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;



public class MovieFragment extends Fragment implements MoviePlayer.PlayerFeedback {

    private static final String TAG = "PLAYER";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    SurfaceView mSurfaceView;
    Surface surface;
    Button buttonPlay;
    Button buttonStop;
    Button buttonPause;
    Button buttonFastForward;
    Button buttonRewind;

    File selectedFile;

    ProgressBar pb;
    ProgressBarThread pbt;

    MoviePlayer player = null;
    //Audio a;

    public boolean inited = false;
    private boolean show = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view= inflater.inflate(R.layout.fragment_fragment2, container, false);
        mSurfaceView = view.findViewById(R.id.movie_surface_view);

        pb = view.findViewById(R.id.progressBar);
        pb.setBackgroundColor(Color.WHITE);

        buttonPlay = view.findViewById(R.id.button1);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickPlay();
            }
        });
        buttonPause = view.findViewById(R.id.button2);
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });
        buttonStop = view.findViewById(R.id.button3);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayback();
            }
        });
        buttonRewind = view.findViewById(R.id.button6);
        buttonRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rewind();
            }
        });
        buttonFastForward = view.findViewById(R.id.button4);
        buttonFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fastForward();
            }
        });

        LinearLayout rlayout = view.findViewById(R.id.mainlayout);
        rlayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(show){
                    LinearLayout one = view.findViewById(R.id.one);
                    one.setVisibility(View.VISIBLE);
                }
            }

        });
        return view;
    }

    public void updateSelectedFile(String path) {
        selectedFile = new File(path);
        stopPlayback();
        clickPlay();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "PlayMovieActivity onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "PlayMovieActivity onPause");
        super.onPause();
    }

    public void clickPlay() {
        show = true;
        if(inited) {
            player.pressedPlay();
            return;
        }

        inited = true;
        surface = mSurfaceView.getHolder().getSurface();
        setVideoVisibility(true);
        try {
            Activity activity = getActivity();
            assert activity != null;
            int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }
            player = new MoviePlayer(selectedFile, surface, this);
            if (player.getExtractor()==null)
            {
                Context context;
                context= getActivity().getApplicationContext();
                CharSequence text = "Error with playback!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                mSurfaceView.dispatchFinishTemporaryDetach();
                player.resetmPlayTask();
                player = null;
                pbt = null;
                inited = false;
                setVideoVisibility(false);
                return;
            }

        } catch (IOException ioe) {
            return;
        }

        pbt= new ProgressBarThread(pb,player);
        pbt.start();
        player.start();
    }

    private void setVideoVisibility(boolean status) {
        mSurfaceView.setVisibility(status ? View.VISIBLE : View.INVISIBLE);
    }

    public void fastForward() {
        if (inited) {
            player.pressedFastForward();
        }
    }

    public void pause() {
        if (inited) {
            player.pressedPause();
        }
    }

    public void rewind() {
        if (inited) {
           player.pressedRewind();
        }
    }


    public void stopPlayback() {
        if (player!=null) {
            player.stopPlayback();

            if (player.getmPlayTask()) {
                pbt.requestStop();
                mSurfaceView.dispatchFinishTemporaryDetach();

                if (player.pressedStop()) {
                    player = null;
                }
                inited = false;
            }
            setVideoVisibility(false);
        }
    }

    @Override
    public void playbackStopped() {
    }
}