package com.example.smarija.mediaplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaFormat;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.support.v4.app.ActivityCompat;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;


import java.io.File;
import java.io.IOException;


public class Fragment2 extends Fragment {
// implements OnItemSelectedListener, View.OnClickListener,
//        TextureView.SurfaceTextureListener, MoviePlayer.PlayerFeedback {
    private static final String TAG = "PLAYER";
    TextView textView;
    private TextureView mTextureView;

    private boolean mShowStopLabel;
    private MoviePlayer.PlayTask mPlayTask;
    private boolean mSurfaceTextureReady = false;
    public MediaFormat ff;
    SpeedControlCallback callback;

    ProgressBar pb;
    ProgressBarThread pbt;
    MoviePlayer player = null;
    //Audio a;
    Surface surface;

    public boolean inited = false;
    public boolean stop_ff = false;



    //za otvaranje iz fajla:
    private static final int REQUEST_PICK_FILE = 1;
    private TextView filePath;
    private Button Browse;
    private File selectedFile;

    private boolean show = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        final View view= inflater.inflate(R.layout.fragment_fragment2, container, false);

//        mTextureView = view.findViewById(R.id.movie_texture_view);
//        mTextureView.setSurfaceTextureListener(this);
//
//        pb = view.findViewById(R.id.progressBar);
//        pb.setBackgroundColor(Color.WHITE);
//
//       filePath = view.findViewById(R.id.file_path);
//        Browse = view.findViewById(R.id.browse);
//        Browse.setOnClickListener(this);
//
//        LinearLayout rlayout = view.findViewById(R.id.mainlayout);
//        rlayout.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if(show){
//                    LinearLayout one = view.findViewById(R.id.one);
//                    one.setVisibility(View.VISIBLE);
//                }
//            }
//
//        });


        textView = view.findViewById(R.id.textView);
        return view;
    }

    public void updateText(String text) {
        Log.e("PROBA2",text);

        textView.setText(text);
    }


//    @Override
//    public void onClick(View v) {
//
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
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if(resultCode == RESULT_OK){
//            switch (requestCode){
//                case REQUEST_PICK_FILE:
//                    if(data.hasExtra(FilePicker.EXTRA_FILE_PATH)){
//                        selectedFile = new File(data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
//                        filePath.setText(selectedFile.getPath());
//                    }
//                    break;
//            }
//        }
//
//    }
//
//
//    @Override
//    protected void onResume() {
//        Log.d(TAG, "PlayMovieActivity onResume");
//        super.onResume();
//    }
//
//    @Override
//    protected void onPause() {
//        Log.d(TAG, "PlayMovieActivity onPause");
//        super.onPause();
//
//        if (mPlayTask != null) {
//            mPlayTask.waitForStop();
//        }
//    }
//
//    @Override
//    public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
//        Log.d(TAG, "SurfaceTexture ready (" + width + "x" + height + ")");
//        mSurfaceTextureReady = true;
//    }
//
//    @Override
//    public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {}
//
//    @Override
//    public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
//        mSurfaceTextureReady = false;
//        return true;
//    }
//
//    @Override
//    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
//
//    @Override
//    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {}
//
//    @Override public void onNothingSelected(AdapterView<?> parent) {}
////
//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//    private static String[] PERMISSIONS_STORAGE = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };
//
//    public void clickPlay(@SuppressWarnings("unused") View unused) {
//        LinearLayout one = (LinearLayout) findViewById(R.id.one);
//        one.setVisibility(View.INVISIBLE);
//        show = true;
//        if(inited) {
//            if (player.paused) {
//                //a.paused = false;
//                player.paused = false;
//                callback.setFixedPlaybackRate(0);
//                player.mFrameCallback.resetTime();
//                return;
//            } else if (player.fastForward) {
//                long temp = player.temp();
//                temp = (long) (temp + 500000);
//                //a.extractor.seekTo(temp, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//                player.fastForward = false;
//                //a.fastforward = false;
//                player.mFrameCallback.resetTime();
//                callback.setFixedPlaybackRate(0);
//                player.mFrameCallback.resetTime();
//                return;
//            }
//            else if (player.rewind) {
//                long temp = player.temp();
//                temp = (long) (temp + 500000);
//                //a.extractor.seekTo(temp, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//                player.rewind = false;
//                //a.rewind = false;
//                player.mFrameCallback.resetTime();
//                callback.setFixedPlaybackRate(0);
//                return;
//            } else
//                return;
//        }
//
//            inited = true;
//        {
//            callback = new SpeedControlCallback();
//            SurfaceTexture st = mTextureView.getSurfaceTexture();
//            surface = new Surface(st);
//            try {
////                if(selectedFile == null)
////                    selectedFile = new File("/sdcard/Movies/big_buck_bunny.mp4");
//                // Check if we have write permission
//                Activity activity = (Activity) Fragment2.this;
//                int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//                if (permission != PackageManager.PERMISSION_GRANTED) {
//                    // We don't have permission so prompt the user
//                    ActivityCompat.requestPermissions(
//                            activity,
//                            PERMISSIONS_STORAGE,
//                            REQUEST_EXTERNAL_STORAGE
//                    );
//                }
//                player = new MoviePlayer(selectedFile, surface, callback);
//            } catch (IOException ioe) {
//                surface.release();
//                return;
//            }
//            adjustAspectRatio(player.getVideoWidth(), player.getVideoHeight());
//            //play video and audio
//            mPlayTask = new MoviePlayer.PlayTask(player, this);
//            //a = new Audio();
//            if(selectedFile == null){
//                //a.useDescriptop = true;
//                //a.mfis=this.getResources().openRawResource(R.raw.test);
//                //a.msampleFD = getResources().openRawResourceFd(R.raw.big_buck_bunny);
//            }
//            else {
//                //a.fis = selectedFile;
//                //a.sampleFD = selectedFile;
//            }
//            //a.start();
//
//            mShowStopLabel = true;
//            pbt= new ProgressBarThread(pb,player);
//            pbt.start();
//            mPlayTask.execute();
//        }
//    }
//
//    public void fastForward(@SuppressWarnings("unused") View unused) {
//
//        if (inited) {
//            if(player.rewind==true){
//                player.rewind=false;
//                //a.rewind = false;
//            }
//            if (player.paused) {
//                player.paused = false;
//               // a.paused = false;
//            }
//
//            callback.setFixedPlaybackRate(120);
//            player.fastForward = true;
//            //a.fastforward = true;
//        } else {
//        }
//    }
//
//    public void pause(@SuppressWarnings("unused") View unused) {
//        if (inited) {
//            //a.paused = true;
//            player.paused = true;
//            if (player.rewind) {
//                player.rewind = false;
//                callback.setFixedPlaybackRate(0);
//                player.mFrameCallback.resetTime();
//                return;
//            } else {
//            }
//        }
//    }
//
//    public void rewind(@SuppressWarnings("unused") View unused) {
//        if (inited) {
//            if(player.fastForward==true){
//                player.fastForward=false;
//                //a.fastforward = false;
//            }
//            if (player.paused) {
//                player.paused = false;
//                //a.paused = false;
//            }
//        callback.setFixedPlaybackRate(120);
//        player.rewind=true;
//        //a.rewind = true;
//        }
//    }
//
//
//    public void stopPlayback(@SuppressWarnings("unused") View unused) {
//        if (stop_ff == true) {
//        } else
//        {
//            stop_ff = true;
//            if (mPlayTask != null ) {
//                if (player.paused) {
//                    player.paused = false;
//                    //a.paused=false;
//                    mPlayTask.requestStop();
//                    //a.requestStop();
//                    pbt.requestStop();
//                    surface.release();
//                    inited = false;
//                } else {
//                    mPlayTask.requestStop();
//                    //a.requestStop();
//                    pbt.requestStop();
//                    //a.exit();
//                    surface.release();
//                    mPlayTask = null;
//                    player = null;
//                    pbt = null;
//                    inited = false;
//                }
//            }
//            stop_ff = false;
//        }
//    }
//
//    @Override
//    public void playbackStopped() {
//
//    }
//
//    /**
//     * Sets the TextureView transform to preserve the aspect ratio of the video.
//     */
//    private void adjustAspectRatio(int videoWidth, int videoHeight) {
//        int viewWidth = mTextureView.getWidth();
//        int viewHeight = mTextureView.getHeight();
//        double aspectRatio = (double) videoHeight / videoWidth;
//
//        int newWidth, newHeight;
//        if (viewHeight > (int) (viewWidth * aspectRatio)) {
//            // limited by narrow width; restrict height
//            newWidth = viewWidth;
//            newHeight = (int) (viewWidth * aspectRatio);
//        } else {
//            // limited by short height; restrict width
//            newWidth = (int) (viewHeight / aspectRatio);
//            newHeight = viewHeight;
//        }
//        int xoff = (viewWidth - newWidth) / 2;
//        int yoff = (viewHeight - newHeight) / 2;
//        Log.v(TAG, "video=" + videoWidth + "x" + videoHeight +
//                " view=" + viewWidth + "x" + viewHeight +
//                " newView=" + newWidth + "x" + newHeight +
//                " off=" + xoff + "," + yoff);
//
//        Matrix txform = new Matrix();
//        mTextureView.getTransform(txform);
//        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
//        txform.postTranslate(xoff, yoff);
//        mTextureView.setTransform(txform);
//    }
//
//
//    @Override
//    public void onBackPressed()
//    {
//        //a.exit();
//        finish();
//    }


}