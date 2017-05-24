package com.example.smarija.mediaplayer;

import android.app.Fragment;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Choreographer;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.example.smarija.mediaplayer.R;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by smarija on 10.4.2017.
 */

public class Codec extends Fragment {

    String LOG_TAG = "Player";
    private static final boolean VERBOSE = true;

    String TAG = "Player";

    private static final int TIMEOUT_USEC = 10000; //How long to wait for the next buffer to become available

    public MediaCodec codec;
    public MediaExtractor extractor;
    public MediaFormat format;

    private boolean pause_flag = false;
    private boolean stop_flag = false;
    private boolean ff_flag = false;
    private boolean fr_flag = false;
    boolean isEOS = false;
    private boolean mLoop = false;
    private Surface mSurface;
    public SurfaceView sview;
    private AssetFileDescriptor mcAssetFileDescriptor;
    FrameCallback mFrameCallback;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    public int size;
    public long file_size=0;
    public long current_size=0;
    private long mcpresentationTimeUs;
    private long previous_mcpresentationTimeUs=-1;
    private int videoIndex;

    private com.example.smarija.mediaplayer.SpeedControlCallback sp = new com.example.smarija.mediaplayer.SpeedControlCallback();

    private MediaExtractor createExtractor() throws IOException{
        MediaExtractor e;
        mcAssetFileDescriptor = getResources().openRawResourceFd(R.raw.big_buck_bunny);
        try {
            e = new MediaExtractor();
            e.setDataSource(
                    mcAssetFileDescriptor.getFileDescriptor(),
                    mcAssetFileDescriptor.getStartOffset(),
                    mcAssetFileDescriptor.getLength());
            file_size = mcAssetFileDescriptor.getLength()-mcAssetFileDescriptor.getStartOffset();
        } catch (IOException io) {
            Log.d(LOG_TAG, "Can't find / Open");
            return null;
        } finally {
            try {
                mcAssetFileDescriptor.close();
            } catch (IOException io) {
                Log.d(LOG_TAG, "Can't close assetFileDescriptor");
            }
        }
        return e;
    }


    private void doExtract(MediaExtractor extractor, int trackIndex, MediaCodec decoder,
                           FrameCallback frameCallback) {


        final int TIMEOUT_USEC = 10000;

        int inputChunk = 0;
        long firstInputTimeNsec = -1;

        boolean outputDone = false;
        boolean inputDone = false;
        while (!outputDone) {
            if (VERBOSE) Log.d(TAG, "loop");
//            if (0) {
//                Log.d(TAG, "Stop requested");
//                return;
//            }

            // Feed more data to the decoder.
            if (!inputDone) {
                int inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC);
                ByteBuffer decoderInputBuffers = decoder.getInputBuffer(inputBufIndex);
                if (inputBufIndex >= 0) {
                    if (firstInputTimeNsec == -1) {
                        firstInputTimeNsec = System.nanoTime();
                    }
                    ByteBuffer inputBuf = decoderInputBuffers;
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
                    if (VERBOSE) Log.d(TAG, "decoder output format changed: " + newFormat);
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
                    if (doRender && frameCallback != null) {
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


    public interface FrameCallback {
        /**
         * Called immediately before the frame is rendered.
         * @param presentationTimeUsec The desired presentation time, in microseconds.
         */
        void preRender(long presentationTimeUsec);

        /**
         * Called immediately after the frame render call returns.  The frame may not have
         * actually been rendered yet.
         * TODO: is this actually useful?
         */
        void postRender();

        /**
         * Called after the last frame of a looped movie has been rendered.  This allows the
         * callback to adjust its expectations of the next presentation time stamp.
         */
        void loopReset();
    }

    private void selectTrack(MediaExtractor extractor) throws IOException {
        int numtracks = extractor.getTrackCount();
        for (int i = 0; i < numtracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                videoIndex = i;
            }
        }
    }

    private boolean isVideoFeatureSupported(String mimeType, String feature) {
        MediaFormat format = MediaFormat.createVideoFormat(mimeType, 1920, 1080);
        format.setFeatureEnabled(feature, true);

        MediaCodecList mcl = new MediaCodecList(MediaCodecList.ALL_CODECS);
        String codecName = mcl.findDecoderForFormat(format);
        return (codecName == null) ? false : true;
    }

    private void releaseAsset() {
        if (codec != null) {
            codec.stop();
            codec.release();
            codec = null;
        }
        if (extractor != null) {
            extractor.release();
            extractor = null;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment, container, false);
        sview = (SurfaceView) view.findViewById(R.id.preview_surface);
        sview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int _format, int width, int height) {    //This is called immediately after any structural changes (format or size) have been made to the surface.
                try {
                    extractor = createExtractor();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                int index = 0;
                try {
                    selectTrack(extractor);
                    index = videoIndex;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                extractor.selectTrack(index);
                format = extractor.getTrackFormat(index);
                if (isVideoFeatureSupported(
                        format.getString(MediaFormat.KEY_MIME),
                        MediaCodecInfo.CodecCapabilities.FEATURE_TunneledPlayback)) {
                    format.setFeatureEnabled(MediaCodecInfo.CodecCapabilities.FEATURE_TunneledPlayback, true);
                    format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
                }

                try {
                    codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
                    if (null == codec) {
                        Log.d(LOG_TAG, "Can't find / Open Video");
                        return;
                    }
                } catch (IOException io) {
                    Log.d(LOG_TAG, "Can't find / Open Video");
                    return;
                }
                mSurface = holder.getSurface();
                codec.configure(format, mSurface, null, 0);

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                releaseAsset();
            }
        });


        return view;
    }


    public void play() {
//        fr_flag = false;
//
//        if (!pause_flag && !stop_flag && !ff_flag && !fr_flag) {
//            codec.start();
//
//
//        } else if (pause_flag) {
//            pause_flag = false;
//        } else if (stop_flag) {
//            stop_flag = false;
//            codec.configure(format, mSurface, null, 0);
//            codec.setCallback(mcCallback);
//            codec.start();
//        } else if (ff_flag) {
//            ff_flag = false;
//        }
        codec.start();
        // doExtract(extractor,videoIndex,codec,sp);
    }

    public void pause() {
        extractor.seekTo(extractor.getSampleTime(), MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        pause_flag = true;
    }

    public void stop() {
        stop_flag = true;
        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        codec.stop();
    }

    public void ff() {
        ff_flag = true;
    }

    public void fr() {
        fr_flag = true;
    }

    public void openFile() {

    }

}
