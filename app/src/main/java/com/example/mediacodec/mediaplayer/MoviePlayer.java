package com.example.mediacodec.mediaplayer;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;


class MoviePlayer {
    private static final String TAG = "PLAYER";

    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    private volatile boolean mIsStopRequested;

    private MediaExtractor extractor;
    private MediaCodec decoder;

    private PlayTask mPlayTask;
    private File mSourceFile;
    private SpeedControlCallback mFrameCallback;
    private boolean mLoop;
    private int mVideoWidth;
    private int mVideoHeight;
    private int trackIndex;
    private String mime;

    private final int TIMEOUT_USEC = 10000;
    private long firstInputTimeNsec = -1;
    private long rewind_timer = -1;
    private boolean outputDone = false;
    private boolean inputDone = false;

    long file_size = 0;
    long current_size = 0;

    enum States{PLAY,PAUSE,FAST_FORWARD,REWIND,STOP}

    private States currentState = States.STOP;

    MediaExtractor getExtractor() {
        return extractor;
    }

    void stopPlayback() {
        currentState = States.STOP;
        requestStop();
        mPlayTask.waitForStop();
    }

    void pressedPause() {
        currentState = States.PAUSE;
        mFrameCallback.setFixedPlaybackRate(0);
        mFrameCallback.resetTime();
    }

    void pressedRewind() {
        currentState = States.REWIND;
        mFrameCallback.setFixedPlaybackRate(120);
    }

    void pressedFastForward() {
        currentState = States.FAST_FORWARD;
        mFrameCallback.setFixedPlaybackRate(120);
    }

    void pressedStop() {
        currentState = States.STOP;
        mPlayTask=null;
    }

    void pressedPlay() {
        currentState = States.PLAY;

//        long temp = temp();
//        temp = (long) (temp + 500000);
        //a.extractor.seekTo(temp, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        //a.fastforward = false;
        mFrameCallback.resetTime();
        mFrameCallback.setFixedPlaybackRate(0);
        mFrameCallback.resetTime();
    }

    void start() {
        currentState=States.PLAY;
        mPlayTask.execute();
    }

    boolean isPlayTaskCreated() {
        return mPlayTask != null;
    }

    void resetmPlayTask() {
        mPlayTask=null;
    }

    long getVideoDuration() {
        return file_size;
    }

    String getVideoFormat() {
        return mime;
    }

    public interface PlayerFeedback {
        void playbackStopped();
    }

    public interface FrameCallback {

        void preRender(long presentationTimeUsec);

        void postRender();

        void loopReset();

    }
    MoviePlayer(File sourceFile, Surface outputSurface, PlayerFeedback feedback) throws IOException {
        this(sourceFile,outputSurface,new SpeedControlCallback(), feedback);
    }

    private MoviePlayer(File sourceFile, Surface outputSurface, SpeedControlCallback frameCallback, PlayerFeedback feedbackPlayTask)
            throws IOException {

        if(sourceFile == null){
            extractor=null;
            return;
        }

        mSourceFile = sourceFile;
        mFrameCallback = frameCallback;
        extractor = null;
        mPlayTask = new PlayTask(this, feedbackPlayTask);

        extractor = new MediaExtractor();
        extractor.setDataSource(sourceFile.toString());
        trackIndex = selectTrack();
        if (trackIndex < 0) {
           extractor=null;
           return;
        }
        extractor.selectTrack(trackIndex);

        MediaFormat format = extractor.getTrackFormat(trackIndex);
        mVideoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
        mVideoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
        file_size = format.getLong(MediaFormat.KEY_DURATION);

        mime = format.getString(MediaFormat.KEY_MIME);
        decoder = MediaCodec.createDecoderByType(mime);
        decoder.configure(format, outputSurface, null, 0);
    }

    int getVideoWidth() {
        return mVideoWidth;
    }

    int getVideoHeight() {
        return mVideoHeight;
    }

    private void setLoopMode(boolean loopMode) {
        mLoop = loopMode;
    }

    private void requestStop() {
        mIsStopRequested = true;
    }

    private void play() throws IOException {
        if (!mSourceFile.canRead()) {
            throw new FileNotFoundException("Unable to read " + mSourceFile);
        }

        try {
            decoder.start();
            doExtract(extractor, decoder, mFrameCallback);
        } finally {
            if (decoder != null) {
                decoder.stop();
                decoder.release();
                decoder = null;
            }
            if (extractor != null) {
                extractor.release();
                extractor = null;
            }
            currentState = States.STOP;
        }
    }


    private int selectTrack() {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            Log.e("IGOR",mime);
            if (mime.startsWith("video/")) {
                return i;
            }
        }

        return -1;
    }


    private void doExtract(MediaExtractor extractor, MediaCodec decoder, FrameCallback frameCallback) {

        new Thread(new Runnable(){
                    public void run(){
                        inputtingDecoder();
                    }
                }
        ).start();

        while (!outputDone) {
            if(currentState == States.PAUSE) {
                if (mIsStopRequested) {
                    Log.d(TAG, "Stop requested");
                    return;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if(currentState == States.REWIND) {
                long rewind_timeout = 20000;
                if (rewind_timer == -1) {
                    rewind_timer = current_size;
                }
                extractor.seekTo(rewind_timer - rewind_timeout, MediaExtractor.SEEK_TO_NEXT_SYNC);
                rewind_timer = rewind_timer - rewind_timeout;
                mFrameCallback.resetTime();
                mFrameCallback.resetTime();
                if ((rewind_timer - rewind_timeout) < 0) {
                        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                }
            }
            else
                rewind_timer=-1;

            if (mIsStopRequested) {
                Log.d(TAG, "Stop requested");
                return;
            }



            int decoderStatus = decoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER || decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                Log.v(TAG,"Nothing to do...skipping");
            } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = decoder.getOutputFormat();
                Log.e(TAG, "GRKI decoder output format changed: " + newFormat);
                //MovieFragment.change(newFormat.getInteger(MediaFormat.));
            } else if (decoderStatus < 0) {
                throw new RuntimeException("unexpected result from decoder.dequeueOutputBuffer: " + decoderStatus);
            } else { // decoderStatus >= 0
                if (firstInputTimeNsec != 0) {
                    // Log the delay from the first buffer of input to the first buffer
                    // of output.
                    long nowNsec = System.nanoTime();
                    Log.d(TAG, "startup lag " + ((nowNsec-firstInputTimeNsec) / 1000000.0) + " ms");
                    firstInputTimeNsec = 0;
                }
                boolean doLoop = false;

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (mLoop) {
                        doLoop = true;
                    } else {
                        outputDone = true;
                    }
                }

                boolean doRender = (mBufferInfo.size != 0);

                // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                // to SurfaceTexture to convert to a texture.  We can't control when it
                // appears on-screen, but we can manage the pace at which we release
                // the buffers.
                if (doRender && frameCallback != null && currentState != States.REWIND) {
                    frameCallback.preRender(mBufferInfo.presentationTimeUs);
                }
                decoder.releaseOutputBuffer(decoderStatus, doRender);
                if (doRender && frameCallback != null) {
                    frameCallback.postRender();
                }

                if (doLoop) {
                    Log.d(TAG, "Reached EOS, looping");
                    extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                    inputDone = false;
                    decoder.flush();    // reset decoder state
                    if (frameCallback != null) {
                        frameCallback.loopReset();
                    }
                }
            }

        }
    }

    private void inputtingDecoder() {

        // Feed more data to the decoder.
        while (!inputDone) {

            if (mIsStopRequested) {
                Log.d(TAG, "Stop requested");
                return;
            }

            int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
            if (inputBufIndex >= 0) {
                if (firstInputTimeNsec == -1) {
                    firstInputTimeNsec = System.nanoTime();
                }
                ByteBuffer inputBuf = decoder.getInputBuffer(inputBufIndex);
                // Read the sample data into the ByteBuffer.  This neither respects nor
                // updates inputBuf's position, limit, etc.
                assert inputBuf != null;
                int chunkSize = extractor.readSampleData(inputBuf, 0);
                if (chunkSize < 0) {
                    // End of stream -- send empty frame with EOS flag set.
                    decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                    inputDone = true;
                } else {
                    if (extractor.getSampleTrackIndex() != trackIndex) {
                        Log.w(TAG, "WEIRD: got sample from track " +
                                extractor.getSampleTrackIndex() + ", expected " + trackIndex);
                    }

                    long presentationTimeUs = extractor.getSampleTime();
                    decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                            presentationTimeUs, 0 /*flags*/);
                    extractor.advance();
                    current_size = extractor.getSampleTime();
                }
            }
        }
    }

//    private long temp(){
//        return mBufferInfo.presentationTimeUs;
//    }



    public static class PlayTask implements Runnable {
        private static final int MSG_PLAY_STOPPED = 0;

        private MoviePlayer mPlayer;
        private PlayerFeedback mFeedback;
        private boolean mDoLoop=false;
        private Thread mThread;
        private LocalHandler mLocalHandler;

        private final Object mStopLock = new Object();
        private boolean mStopped = false;

        PlayTask(MoviePlayer player, PlayerFeedback feedback) {
            mPlayer = player;
            mFeedback = feedback;
            mLocalHandler = new LocalHandler();
        }

        void execute() {
            mPlayer.setLoopMode(mDoLoop);
            mThread = new Thread(this, "Movie Player");
            mThread.start();
        }

        void waitForStop() {
            synchronized (mStopLock) {
                while (!mStopped) {
                    try {
                        mStopLock.wait();
                    } catch (InterruptedException ie) {
                        // discard
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                mPlayer.play();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            } finally {
                // tell anybody waiting on us that we're done
                synchronized (mStopLock) {
                    mStopped = true;
                    mStopLock.notifyAll();
                }

                // Send message through Handler so it runs on the right thread.
                mLocalHandler.sendMessage(
                        mLocalHandler.obtainMessage(MSG_PLAY_STOPPED, mFeedback));
            }
        }

        private static class LocalHandler extends Handler {
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;

                if (what == MSG_PLAY_STOPPED) {
                    PlayerFeedback fb = (PlayerFeedback) msg.obj;
                    fb.playbackStopped();
                } else {
                    throw new RuntimeException("Unknown msg " + what);
                }
            }
        }
    }
}