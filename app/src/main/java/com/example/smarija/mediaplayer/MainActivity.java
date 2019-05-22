package com.example.smarija.mediaplayer;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.File;

import static com.example.smarija.mediaplayer.R.id.fragment2;


public class MainActivity extends Activity implements Communicate {
// implements AdapterView.OnItemSelectedListener, View.OnClickListener,
//            TextureView.SurfaceTextureListener, MoviePlayer.PlayerFeedback {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);


            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }


            File Directory = new File("/mnt/sdcard");

            Log.e("IGOR", "Directory in MainActivity  "+String.valueOf(Directory.listFiles()==null));


//            mTextureView = (TextureView) findViewById(R.id.movie_texture_view);
//            mTextureView.setSurfaceTextureListener(this);
//
//            pb = (ProgressBar) findViewById(R.id.progressBar);
//            pb.setBackgroundColor(Color.WHITE);
//
//            filePath = (TextView) findViewById(R.id.file_path);
//            Browse = (Button) findViewById(R.id.browse);
//            Browse.setOnClickListener(this);
//
//            LinearLayout rlayout = (LinearLayout) findViewById(R.id.mainlayout);
//            rlayout.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    if(show){
//                        LinearLayout one = (LinearLayout) findViewById(R.id.one);
//                        one.setVisibility(View.VISIBLE);
//                    }
//                }
//
//            });

        }

    @Override
    public void sendText(String s) {
        Fragment2 frag=(Fragment2) getFragmentManager().findFragmentById(fragment2);
        frag.updateText(s);
    }


//        @Override
//        public void onClick(View v) {
//
//            switch (v.getId()){
//                case R.id.browse:
//                    if(!inited || stop_ff == true ) {
//                        Intent intent = new Intent(this, FilePicker.class);
//                        startActivityForResult(intent, REQUEST_PICK_FILE);
//                    }
//                    else{
//                        stopPlayback(v);
//                        Intent intent = new Intent(this, FilePicker.class);
//                        startActivityForResult(intent, REQUEST_PICK_FILE);
//                    }
//                    break;
//            }
//        }
//
//        @Override
//        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//            if(resultCode == RESULT_OK){
//                switch (requestCode){
//                    case REQUEST_PICK_FILE:
//                        if(data.hasExtra(FilePicker.EXTRA_FILE_PATH)){
//                            selectedFile = new File(data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
//                            filePath.setText(selectedFile.getPath());
//                        }
//                        break;
//                }
//            }
//
//        }
//
//
//        @Override
//        protected void onResume() {
//            Log.d(TAG, "PlayMovieActivity onResume");
//            super.onResume();
//        }
//
//        @Override
//        protected void onPause() {
//            Log.d(TAG, "PlayMovieActivity onPause");
//            super.onPause();
//
//            if (mPlayTask != null) {
//                mPlayTask.waitForStop();
//            }
//        }
//
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
//            Log.d(TAG, "SurfaceTexture ready (" + width + "x" + height + ")");
//            mSurfaceTextureReady = true;
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture st, int width, int height) {}
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture st) {
//            mSurfaceTextureReady = false;
//            return true;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
//
//        @Override
//        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {}
//
//        @Override public void onNothingSelected(AdapterView<?> parent) {}
//

//
//        public void clickPlay(@SuppressWarnings("unused") View unused) {
//            LinearLayout one = (LinearLayout) findViewById(R.id.one);
//            one.setVisibility(View.INVISIBLE);
//            show = true;
//            if(inited) {
//                if (player.paused) {
//                    //a.paused = false;
//                    player.paused = false;
//                    callback.setFixedPlaybackRate(0);
//                    player.mFrameCallback.resetTime();
//                    return;
//                } else if (player.fastForward) {
//                    long temp = player.temp();
//                    temp = (long) (temp + 500000);
//                    //a.extractor.seekTo(temp, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//                    player.fastForward = false;
//                    //a.fastforward = false;
//                    player.mFrameCallback.resetTime();
//                    callback.setFixedPlaybackRate(0);
//                    player.mFrameCallback.resetTime();
//                    return;
//                }
//                else if (player.rewind) {
//                    long temp = player.temp();
//                    temp = (long) (temp + 500000);
//                    //a.extractor.seekTo(temp, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
//                    player.rewind = false;
//                    //a.rewind = false;
//                    player.mFrameCallback.resetTime();
//                    callback.setFixedPlaybackRate(0);
//                    return;
//                } else
//                    return;
//            }
//
//            inited = true;
//            {
//                callback = new SpeedControlCallback();
//                SurfaceTexture st = mTextureView.getSurfaceTexture();
//                surface = new Surface(st);
//                try {
////                if(selectedFile == null)
////                    selectedFile = new File("/sdcard/Movies/big_buck_bunny.mp4");
//                    // Check if we have write permission
//                    Activity activity = (Activity) Fragment2.this;

//                    player = new MoviePlayer(selectedFile, surface, callback);
//                } catch (IOException ioe) {
//                    surface.release();
//                    return;
//                }
//                adjustAspectRatio(player.getVideoWidth(), player.getVideoHeight());
//                //play video and audio
//                mPlayTask = new MoviePlayer.PlayTask(player, this);
//                //a = new Audio();
//                if(selectedFile == null){
//                    //a.useDescriptop = true;
//                    //a.mfis=this.getResources().openRawResource(R.raw.test);
//                    //a.msampleFD = getResources().openRawResourceFd(R.raw.big_buck_bunny);
//                }
//                else {
//                    //a.fis = selectedFile;
//                    //a.sampleFD = selectedFile;
//                }
//                //a.start();
//
//                mShowStopLabel = true;
//                pbt= new ProgressBarThread(pb,player);
//                pbt.start();
//                mPlayTask.execute();
//            }
//        }
//
//        public void fastForward(@SuppressWarnings("unused") View unused) {
//
//            if (inited) {
//                if(player.rewind==true){
//                    player.rewind=false;
//                    //a.rewind = false;
//                }
//                if (player.paused) {
//                    player.paused = false;
//                    // a.paused = false;
//                }
//
//                callback.setFixedPlaybackRate(120);
//                player.fastForward = true;
//                //a.fastforward = true;
//            } else {
//            }
//        }
//
//        public void pause(@SuppressWarnings("unused") View unused) {
//            if (inited) {
//                //a.paused = true;
//                player.paused = true;
//                if (player.rewind) {
//                    player.rewind = false;
//                    callback.setFixedPlaybackRate(0);
//                    player.mFrameCallback.resetTime();
//                    return;
//                } else {
//                }
//            }
//        }
//
//        public void rewind(@SuppressWarnings("unused") View unused) {
//            if (inited) {
//                if(player.fastForward==true){
//                    player.fastForward=false;
//                    //a.fastforward = false;
//                }
//                if (player.paused) {
//                    player.paused = false;
//                    //a.paused = false;
//                }
//                callback.setFixedPlaybackRate(120);
//                player.rewind=true;
//                //a.rewind = true;
//            }
//        }
//
//
//        public void stopPlayback(@SuppressWarnings("unused") View unused) {
//            if (stop_ff == true) {
//            } else
//            {
//                stop_ff = true;
//                if (mPlayTask != null ) {
//                    if (player.paused) {
//                        player.paused = false;
//                        //a.paused=false;
//                        mPlayTask.requestStop();
//                        //a.requestStop();
//                        pbt.requestStop();
//                        surface.release();
//                        inited = false;
//                    } else {
//                        mPlayTask.requestStop();
//                        //a.requestStop();
//                        pbt.requestStop();
//                        //a.exit();
//                        surface.release();
//                        mPlayTask = null;
//                        player = null;
//                        pbt = null;
//                        inited = false;
//                    }
//                }
//                stop_ff = false;
//            }
//        }
//
//        @Override
//        public void playbackStopped() {
//
//        }
//
//        /**
//         * Sets the TextureView transform to preserve the aspect ratio of the video.
//         */
//        private void adjustAspectRatio(int videoWidth, int videoHeight) {
//            int viewWidth = mTextureView.getWidth();
//            int viewHeight = mTextureView.getHeight();
//            double aspectRatio = (double) videoHeight / videoWidth;
//
//            int newWidth, newHeight;
//            if (viewHeight > (int) (viewWidth * aspectRatio)) {
//                // limited by narrow width; restrict height
//                newWidth = viewWidth;
//                newHeight = (int) (viewWidth * aspectRatio);
//            } else {
//                // limited by short height; restrict width
//                newWidth = (int) (viewHeight / aspectRatio);
//                newHeight = viewHeight;
//            }
//            int xoff = (viewWidth - newWidth) / 2;
//            int yoff = (viewHeight - newHeight) / 2;
//            Log.v(TAG, "video=" + videoWidth + "x" + videoHeight +
//                    " view=" + viewWidth + "x" + viewHeight +
//                    " newView=" + newWidth + "x" + newHeight +
//                    " off=" + xoff + "," + yoff);
//
//            Matrix txform = new Matrix();
//            mTextureView.getTransform(txform);
//            txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
//            txform.postTranslate(xoff, yoff);
//            mTextureView.setTransform(txform);
//        }
//
//
//        @Override
//        public void onBackPressed()
//        {
//            //a.exit();
//            finish();
//        }
}

