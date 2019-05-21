package com.example.smarija.mediaplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;


public class MoviePlayer {
    private static final String TAG = "PLAYER";
    private static final boolean VERBOSE = false;

    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    private volatile boolean mIsStopRequested;

    public MediaExtractor extractor;
    public MediaCodec decoder;

    private File mSourceFile;
    private Surface mOutputSurface;
    FrameCallback mFrameCallback;
    private boolean mLoop;
    private int mVideoWidth;
    private int mVideoHeight;

    private long rewind_timeout = 20000;
    public boolean flag_stop = false;

    public long file_size = 0;
    public long current_size = 0;

    public boolean flag_play = false;
    public boolean paused = false;
    public boolean fastForward = false;
    public boolean rewind = false;

    public interface PlayerFeedback {
        void playbackStopped();
    }

    public interface FrameCallback {

        void preRender(long presentationTimeUsec);

        void postRender();

        void loopReset();

        void resetTime();
    }


    public MoviePlayer(File sourceFile, Surface outputSurface, FrameCallback frameCallback)
            throws IOException {
        mSourceFile = sourceFile;
        mOutputSurface = outputSurface;
        mFrameCallback = frameCallback;

        extractor = null;



        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(sourceFile.toString());
            Log.e("IGOR",sourceFile.toString());
            int trackIndex = selectTrack();
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + mSourceFile);
            }
            extractor.selectTrack(trackIndex);

            MediaFormat format = extractor.getTrackFormat(trackIndex);
            mVideoWidth = format.getInteger(MediaFormat.KEY_WIDTH);
            mVideoHeight = format.getInteger(MediaFormat.KEY_HEIGHT);
            file_size = format.getLong(MediaFormat.KEY_DURATION);
            Log.d(TAG,"DUZINA JE: "+file_size);
            if (VERBOSE) {
                Log.d(TAG, "Video size is " + mVideoWidth + "x" + mVideoHeight);
            }
        } finally {
            if (extractor != null) {
                extractor.release();
            }
        }
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public void setLoopMode(boolean loopMode) {
        mLoop = loopMode;
    }


    public void requestStop() {
        mIsStopRequested = true;
    }

    public void play() throws IOException {
        flag_play = true;
        extractor = null;
        decoder = null;

        if (!mSourceFile.canRead()) {
            throw new FileNotFoundException("Unable to read " + mSourceFile);
        }

        try {
            extractor = new MediaExtractor();
            extractor.setDataSource(mSourceFile.toString());
            int trackIndex = selectTrack();
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + mSourceFile);
            }
            extractor.selectTrack(trackIndex);

            MediaFormat format = extractor.getTrackFormat(trackIndex);

            String mime = format.getString(MediaFormat.KEY_MIME);
            decoder = MediaCodec.createDecoderByType(mime);
            decoder.configure(format, mOutputSurface, null, 0);
            decoder.start();

            doExtract(extractor, trackIndex, decoder, mFrameCallback);
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
                if (VERBOSE) {
                    Log.e("IGOR", "Extractor selected track " + i + " (" + mime + "): " + format);
                }
                return i;
            }
        }

        return -1;
    }


    private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder,
                           FrameCallback frameCallback) {

        final int TIMEOUT_USEC = 10000;
        ByteBuffer[] decoderInputBuffers = decoder.getInputBuffers();
        int inputChunk = 0;
        long firstInputTimeNsec = -1;
        long rewind_timer = -1;
        boolean outputDone = false;
        boolean inputDone = false;
        while (!outputDone) {
            if (VERBOSE) Log.d(TAG, "loop");
            if(paused)
            {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if(rewind) {
                if (!flag_stop) {
                    if (rewind_timer == -1) {
                        rewind_timer = current_size;
                    }
                    extractor.seekTo(rewind_timer - rewind_timeout, MediaExtractor.SEEK_TO_NEXT_SYNC);
                    rewind_timer = rewind_timer - rewind_timeout;
                    mFrameCallback.resetTime();
                    mFrameCallback.resetTime();
                }
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

            // Feed more data to the decoder.
            if (!inputDone) {
                int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    if (firstInputTimeNsec == -1) {
                        firstInputTimeNsec = System.nanoTime();
                    }
                    ByteBuffer inputBuf = decoderInputBuffers[inputBufIndex];
                    // Read the sample data into the ByteBuffer.  This neither respects nor
                    // updates inputBuf's position, limit, etc.
                    int chunkSize = extractor.readSampleData(inputBuf, 0);
                    if (chunkSize < 0) {
                        // End of stream -- send empty frame with EOS flag set.
                        decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);

                        inputDone = true;
                        if (VERBOSE) Log.d(TAG, "sent input EOS");
                    } else {
                        if (extractor.getSampleTrackIndex() != trackIndex) {
                            Log.w(TAG, "WEIRD: got sample from track " +
                                    extractor.getSampleTrackIndex() + ", expected " + trackIndex);
                        }

                        long presentationTimeUs = extractor.getSampleTime();
                        decoder.queueInputBuffer(inputBufIndex, 0, chunkSize,
                                presentationTimeUs, 0 /*flags*/);
                        if (VERBOSE) {
                            Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                                    chunkSize);
                        }
                        inputChunk++;
                        extractor.advance();
                        int wigor = decoder.getOutputFormat().getInteger(MediaFormat.KEY_WIDTH);
                        int wigo2r = decoder.getInputFormat().getInteger(MediaFormat.KEY_WIDTH);
                        int higor = decoder.getOutputFormat().getInteger(MediaFormat.KEY_HEIGHT);
                        int higo2r = decoder.getInputFormat().getInteger(MediaFormat.KEY_HEIGHT);
                        Log.e("IGOR","Width is: "+wigor+" a drugi je "+wigo2r+"| Height is"+higor+" a drugi je" +higo2r);
                        current_size = extractor.getSampleTime();
                    }
                } else {
                    if (VERBOSE) Log.d(TAG, "input buffer not available");
                }
            }


            if (!outputDone) {
                int decoderStatus = decoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (VERBOSE) Log.d(TAG, "no output from decoder available");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not important for us, since we're using Surface
                    if (VERBOSE) Log.d(TAG, "decoder output buffers changed");
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat newFormat = decoder.getOutputFormat();
                    Log.e(TAG, "GRKI decoder output format changed: " + newFormat);
                    //Fragment2.change(newFormat.getInteger(MediaFormat.));
                } else if (decoderStatus < 0) {
                    throw new RuntimeException(
                            "unexpected result from decoder.dequeueOutputBuffer: " +
                                    decoderStatus);
                } else { // decoderStatus >= 0
                    if (firstInputTimeNsec != 0) {
                        // Log the delay from the first buffer of input to the first buffer
                        // of output.
                        long nowNsec = System.nanoTime();
                        Log.d(TAG, "startup lag " + ((nowNsec-firstInputTimeNsec) / 1000000.0) + " ms");
                        firstInputTimeNsec = 0;
                    }
                    boolean doLoop = false;
                    if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                            " (size=" + mBufferInfo.size + ")");
                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (VERBOSE) Log.d(TAG, "output EOS");
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
                    if (doRender && frameCallback != null && !rewind) {
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
                        frameCallback.loopReset();
                    }
                }
            }
        }
    }

    public long temp(){
        return mBufferInfo.presentationTimeUs;
    }

    public static class PlayTask implements Runnable {
        private static final int MSG_PLAY_STOPPED = 0;

        private MoviePlayer mPlayer;
        private PlayerFeedback mFeedback;
        private boolean mDoLoop;
        private Thread mThread;
        private LocalHandler mLocalHandler;

        private final Object mStopLock = new Object();
        private boolean mStopped = false;

        public PlayTask(MoviePlayer player, PlayerFeedback feedback) {
            mPlayer = player;
            mFeedback = feedback;

            mLocalHandler = new LocalHandler();
        }

        public void execute() {
            mPlayer.setLoopMode(mDoLoop);
            mThread = new Thread(this, "Movie Player");
            mThread.start();
        }

        public void requestStop() {
            mPlayer.requestStop();
        }

        public void waitForStop() {
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

                switch (what) {
                    case MSG_PLAY_STOPPED:
                        PlayerFeedback fb = (PlayerFeedback) msg.obj;
                        fb.playbackStopped();
                        break;
                    default:
                        throw new RuntimeException("Unknown msg " + what);
                }
            }
        }
    }
}