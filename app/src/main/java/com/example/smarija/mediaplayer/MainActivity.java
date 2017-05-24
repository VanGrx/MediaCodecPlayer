package com.example.smarija.mediaplayer;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.example.smarija.mediaplayer.Codec;

import java.io.File;
import java.io.IOException;
//
//
//
//public class MainActivity extends AppCompatActivity{
//
//    Codec c;
//    Audio a;
//    ProgressBar pb;
//    ProgressBarThread pbt;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        c = (Codec) getFragmentManager().findFragmentById(R.id.fragment_container);
//
//        pb = (ProgressBar) findViewById(R.id.progressBar);
//
//        final Button play_but = (Button) findViewById(R.id.button1);
//        a = new Audio();
//        a.fis=this.getResources().openRawResource(R.raw.test);
//        a.sampleFD = getResources().openRawResourceFd(R.raw.big_buck_bunny);
//        play_but.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
////                a.start();
////                pbt= new ProgressBarThread(pb,c);
////                pbt.start();
//                c.play();
//            }
//        });
//
//        Button pause_but = (Button) findViewById(R.id.button2);
//        pause_but.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                c.pause();
//            }
//        });
//
//        Button stop_but = (Button) findViewById(R.id.button3);
//        stop_but.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                c.stop();
//            }
//        });
//
//        Button ff_but = (Button) findViewById(R.id.button4);
//        ff_but.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                c.ff();
//            }
//        });
//
//        Button fr_but = (Button) findViewById(R.id.button6);
//        fr_but.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                c.fr();
//            }
//        });
//
//        Button open_but = (Button) findViewById(R.id.button5);
//        open_but.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //openFile();
//            }
//        });
//
//
//    }
//
//    @Override
//    protected void onStart(){
//
//        super.onStart();
//
//
//    }
//
//    @Override
//    public void onBackPressed()
//    {
//        a.exit();
//        finish();
//    }
//
//
//}

public class MainActivity extends Activity implements OnItemSelectedListener, View.OnClickListener,
        TextureView.SurfaceTextureListener, MoviePlayer.PlayerFeedback {
    private static final String TAG = "PLAYER";

    private TextureView mTextureView;

    private boolean mShowStopLabel;
    private MoviePlayer.PlayTask mPlayTask;
    private boolean mSurfaceTextureReady = false;

    SpeedControlCallback callback;

    ProgressBar pb;
    ProgressBarThread pbt;
    MoviePlayer player = null;
    Audio a;
    Codec c;
    Surface surface;

    public boolean inited = false;

    int sat =0;


    private final Object mStopper = new Object();

    //za otvaranje iz fajla:
    private static final int REQUEST_PICK_FILE = 1;
    private TextView filePath;
    private Button Browse;
    private File selectedFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextureView = (TextureView) findViewById(R.id.movie_texture_view);
        mTextureView.setSurfaceTextureListener(this);



        pb = (ProgressBar) findViewById(R.id.progressBar);

       filePath = (TextView) findViewById(R.id.file_path);
        Browse = (Button) findViewById(R.id.browse);
        Browse.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.browse:
                Intent intent = new Intent(this, FilePicker.class);
                startActivityForResult(intent, REQUEST_PICK_FILE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_PICK_FILE:
                    if(data.hasExtra(FilePicker.EXTRA_FILE_PATH)){
                        selectedFile = new File(data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                        filePath.setText(selectedFile.getPath());
                    }
                    break;
            }
        }

    }


    @Override
    protected void onResume() {
        Log.d(TAG, "PlayMovieActivity onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "PlayMovieActivity onPause");
        super.onPause();

        if (mPlayTask != null) {
            //stopPlayback();
            mPlayTask.waitForStop();
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
        Log.d(TAG, "SurfaceTexture ready (" + width + "x" + height + ")");
        mSurfaceTextureReady = true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
        mSurfaceTextureReady = false;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {}

    @Override public void onNothingSelected(AdapterView<?> parent) {}

    public void clickPlayStop(@SuppressWarnings("unused") View unused) {

        if(inited)
            if(player.paused)
            {
                a.paused=false;
                player.paused=false;
                player.mFrameCallback.resetTime();
                return;
            }
            else
            if(player.fastForward)
            {
                player.fastForward = false;
                player.mFrameCallback.resetTime();
                callback.setFixedPlaybackRate(0);
                player.mFrameCallback.resetTime();

                return;
            }
            else
            if(player.rewind)
            {
                player.rewind = false;
                player.mFrameCallback.resetTime();
                return;
            }
            else
                return;

        inited = true;

//        if (mShowStopLabel) {
//            Log.d(TAG, "stopping movie");
//            //stopPlayback();
//
//            mShowStopLabel = false;
//        } else
        {
//            if (mPlayTask != null) {
//                Log.w(TAG, "movie already playing");
//                return;
//            }
            Log.d(TAG, "starting movie");
            callback = new SpeedControlCallback();

            SurfaceTexture st = mTextureView.getSurfaceTexture();
            surface = new Surface(st);

            //String path = "android.resource://" + getPackageName() + "/" + R.raw.big_buck_bunny;

            try {
                player = new MoviePlayer(selectedFile, surface, callback);
            } catch (IOException ioe) {
                Log.e(TAG, "Unable to play movie", ioe);
                surface.release();
                return;
            }
            adjustAspectRatio(player.getVideoWidth(), player.getVideoHeight());

            //play video and audio
            mPlayTask = new MoviePlayer.PlayTask(player, this);
            a = new Audio();
            a.fis=selectedFile;
            a.sampleFD = selectedFile;
            a.start();

            mShowStopLabel = true;
            pbt= new ProgressBarThread(pb,player);
            pbt.start();
            mPlayTask.execute();


        }
    }

    public void fastForward(@SuppressWarnings("unused") View unused) {
        callback.setFixedPlaybackRate(120);
        player.fastForward = true;
    }

    public void pause(@SuppressWarnings("unused") View unused) {
        a.paused=true;
        player.paused=true;
    }

    public void rewind(@SuppressWarnings("unused") View unused) {
        player.rewind=true;
    }

    public void stopPlayback(@SuppressWarnings("unused") View unused) {
        if (mPlayTask != null) {
            mPlayTask.requestStop();
            a.requestStop();
            pbt.requestStop();
            a.exit();
            surface.release();
            mPlayTask=null;
            player=null;
            pbt=null;
            inited=false;
        }
    }

    @Override
    public void playbackStopped() {

    }


    /**
     * Sets the TextureView transform to preserve the aspect ratio of the video.
     */
    private void adjustAspectRatio(int videoWidth, int videoHeight) {
        int viewWidth = mTextureView.getWidth();
        int viewHeight = mTextureView.getHeight();
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
        mTextureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
        mTextureView.setTransform(txform);
    }

    @Override
    public void onBackPressed()
    {
        a.exit();
        finish();
    }

}