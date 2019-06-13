package com.example.smarija.mediaplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.IOException;



public class Fragment2 extends Fragment implements TextureView.SurfaceTextureListener, View.OnClickListener, MoviePlayer.PlayerFeedback {
// implements OnItemSelectedListener, View.OnClickListener,
//        TextureView.SurfaceTextureListener, MoviePlayer.PlayerFeedback {
    private static final String TAG = "PLAYER";
   // TextView textView;
    private SurfaceView mSurfaceView;

    private MoviePlayer.PlayTask mPlayTask;
    SpeedControlCallback callback;

    ProgressBar pb;
    ProgressBarThread pbt;
    MoviePlayer player = null;
    //Audio a;
    Surface surface;

    Button buttonPlay;
    Button buttonStop;
    Button buttonPause;
    Button buttonFastForward;
    Button buttonRewind;

    public boolean inited = false;
    public boolean stop_ff = false;


    private TextView filePath;
    private File selectedFile;

    private boolean show = false;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        final View view= inflater.inflate(R.layout.fragment_fragment2, container, false);

        mSurfaceView = view.findViewById(R.id.movie_surface_view);

        //mSurfaceView.listener(this);

        pb = view.findViewById(R.id.progressBar);
        pb.setBackgroundColor(Color.WHITE);

        filePath = view.findViewById(R.id.file_path);

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

    public void updateText(String text, String path) {

        selectedFile = new File(path);
        filePath.setText(selectedFile.getPath());
        stopPlayback();
        clickPlay();
    }


    @Override
    public void onClick(View v) {

//        switch (v.getId()){
//            case R.id.browse:
//                if(!inited || stop_ff == true ) {
//                    Intent intent = new Intent(this, FilePicker.class);
//                    startActivityForResult(intent, REQUEST_PICK_FILE);
//                }
//                else{
//                    stopPlayback(v);
//                    Intent intent = new Intent(this, FilePicker.class);
//                    startActivityForResult(intent, REQUEST_PICK_FILE);
//                }
//                break;
//        }
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

        if (mPlayTask != null) {
            mPlayTask.waitForStop();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
        Log.d(TAG, "SurfaceTexture ready (" + width + "x" + height + ")");
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}


//
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public void clickPlay() {
//        LinearLayout one = getActivity().findViewById(R.id.one);
//        one.setVisibility(View.INVISIBLE);
        show = true;
        if(inited) {
            if (player.paused) {
                //a.paused = false;
                player.paused = false;
                callback.setFixedPlaybackRate(0);
                player.mFrameCallback.resetTime();
                return;
            } else if (player.fastForward) {
                long temp = player.temp();
                temp = (long) (temp + 500000);
                //a.extractor.seekTo(temp, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                player.fastForward = false;
                //a.fastforward = false;
                player.mFrameCallback.resetTime();
                callback.setFixedPlaybackRate(0);
                player.mFrameCallback.resetTime();
                return;
            }
            else if (player.rewind) {
                long temp = player.temp();
                temp = (long) (temp + 500000);
                //a.extractor.seekTo(temp, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                player.rewind = false;
                //a.rewind = false;
                player.mFrameCallback.resetTime();
                callback.setFixedPlaybackRate(0);
                return;
            } else
                return;
        }

            inited = true;
        {
            callback = new SpeedControlCallback();
            mSurfaceView.setVisibility(View.VISIBLE);
            surface = mSurfaceView.getHolder().getSurface();
            //surface = new Surface(st);
            try {
//                if(selectedFile == null)
//                    selectedFile = new File("/sdcard/Movies/big_buck_bunny.mp4");
                // Check if we have write permission
                Activity activity = getActivity();
                int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    ActivityCompat.requestPermissions(
                            activity,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
                }
                player = new MoviePlayer(selectedFile, surface, callback);
                if (player.getExtractor()==null)
                {
                    Context context;
                    context= getActivity().getApplicationContext();
                    CharSequence text = "Error with playback!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    mSurfaceView.dispatchFinishTemporaryDetach();
                    //surface.release();
                    mPlayTask = null;
                    player = null;

                    pbt = null;
                    inited = false;
                    refreshAll();
                    return;
                }

            } catch (IOException ioe) {

                //surface.release();
                return;
            }
            adjustAspectRatio(player.getVideoWidth(), player.getVideoHeight());
            //play video and audio
            mPlayTask = new MoviePlayer.PlayTask(player, this);
            //a = new Audio();
            //a.useDescriptop = true;
            //a.mfis=this.getResources().openRawResource(R.raw.test);
            //a.msampleFD = getResources().openRawResourceFd(R.raw.big_buck_bunny);
            //a.fis = selectedFile;
            //a.sampleFD = selectedFile;
            //a.start();

            boolean mShowStopLabel = true;
            pbt= new ProgressBarThread(pb,player);
            pbt.start();
            mPlayTask.execute();
        }
    }

    private void refreshAll() {
        mSurfaceView.setVisibility(View.INVISIBLE);
    }

    public void fastForward() {

        if (inited) {
            if(player.rewind==true){
                player.rewind=false;
            }
            if (player.paused) {
                player.paused = false;
            }

            callback.setFixedPlaybackRate(120);
            player.fastForward = true;
        } else {
        }
    }

    public void pause() {
        if (inited) {
            player.paused = true;
            if (player.rewind) {
                player.rewind = false;
                callback.setFixedPlaybackRate(0);
                player.mFrameCallback.resetTime();
                return;
            }
        }
    }

    public void rewind() {
        if (inited) {
            if(player.fastForward==true){
                player.fastForward=false;
            }
            if (player.paused) {
                player.paused = false;
            }
        callback.setFixedPlaybackRate(120);
        player.rewind=true;
        }
    }


    public void stopPlayback() {
        if (player!=null)
            player.stopPlayback();


        if (stop_ff == true) {
        } else
        {
            stop_ff = true;
            if (mPlayTask != null ) {
                if (player.paused) {
                    player.paused = false;
                    //a.paused=false;
                    mPlayTask.requestStop();
                    //a.requestStop();
                    pbt.requestStop();
                    mSurfaceView.dispatchFinishTemporaryDetach();
                    inited = false;

                } else {
                    mPlayTask.requestStop();
                    pbt.requestStop();
                    mSurfaceView.dispatchFinishTemporaryDetach();
                    mPlayTask = null;
                    player = null;

                   // pbt = null;
                    inited = false;
                }
            }
            stop_ff = false;
        }
        refreshAll();
    }

    @Override
    public void playbackStopped() {
    }

    /**
     * Sets the TextureView transform to preserve the aspect ratio of the video.
     */
    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        int viewWidth = mSurfaceView.getWidth();
        int viewHeight = mSurfaceView.getHeight();
        double aspectRatio = (double) videoHeight / videoWidth;

        int newWidth, newHeight;
        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            // limited by narrow width; restrict height
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            // limited by short height; restrict width
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }
        int xoff = (viewWidth - newWidth) / 2;
        int yoff = (viewHeight - newHeight) / 2;
        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight +
                " view=" + viewWidth + "x" + viewHeight +
                " newView=" + newWidth + "x" + newHeight +
                " off=" + xoff + "," + yoff);

        Matrix txform = new Matrix();
       // mSurfaceView.getHolder().getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
      //  mSurfaceView.setTransform(txform);
    }


}