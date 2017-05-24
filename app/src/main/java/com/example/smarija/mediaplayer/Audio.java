package com.example.smarija.mediaplayer;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTimestamp;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;



public class Audio extends Thread{

    String TAG = "Player";

    public MediaFormat audioFormat;
    public AudioTrack audioTrack;

    boolean stop = false;
    public File fis;

    public boolean paused = false;


    File sampleFD;
    private boolean mIsStopRequested;

    public void exit(){
        stop = true;
    }

    @Override
    public void run() {

        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
        MediaExtractor extractor;
        MediaCodec codec;
        ByteBuffer[] codecInputBuffers;
        ByteBuffer[] codecOutputBuffers;

        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                8192 * 2,
                AudioTrack.MODE_STREAM);

        extractor = new MediaExtractor();
        try {
            extractor.setDataSource(String.valueOf(sampleFD));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int track_index = getAndSelectAudioTrackIndex(extractor);
        MediaFormat format = extractor.getTrackFormat(track_index);
        String mime = format.getString(MediaFormat.KEY_MIME);
        Log.d(TAG, String.format("MIME TYPE: %s", mime));


        try {
            codec = MediaCodec.createDecoderByType(mime);
            codec.configure(format, null /* surface */, null /* crypto */, 0 /* flags */);
            codec.start();
            codecInputBuffers = codec.getInputBuffers();
            codecOutputBuffers = codec.getOutputBuffers();

            extractor.selectTrack(track_index); // <= You must select a track. You will read samples from the media from this track!


            boolean sawInputEOS = false;
            boolean sawOutputEOS = false;

            for (;;) {
                if(stop || sawOutputEOS) {
                    audioTrack.stop();
                    audioTrack.release();
                    break;
                }
                if(paused)
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if (mIsStopRequested) {
                    Log.d(TAG, "Stop requested");
                    return;
                }
                int inputBufIndex = codec.dequeueInputBuffer(-1);
                if (inputBufIndex >= 0) {
                    ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];

                    int sampleSize = extractor.readSampleData(dstBuf, 0);
                    long presentationTimeUs = 0;
                    if (sampleSize < 0) {
                        sawInputEOS = true;
                        sampleSize = 0;
                    } else {
                        presentationTimeUs = extractor.getSampleTime();
                    }

                    codec.queueInputBuffer(inputBufIndex,
                            0, //offset
                            sampleSize,
                            presentationTimeUs,
                            sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                    if (!sawInputEOS) {
                        extractor.advance();
                    }

                    BufferInfo info = new BufferInfo();
                    final int res = codec.dequeueOutputBuffer(info, 1000);
                    if (res >= 0) {
                        int outputBufIndex = res;
                        ByteBuffer buf = codecOutputBuffers[outputBufIndex];

                        final byte[] chunk = new byte[info.size];
                        buf.get(chunk); // Read the buffer all at once
                        buf.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN

                        audioTrack.play();

                        if (chunk.length > 0) {
                            audioTrack.write(chunk, 0, chunk.length);
                        }
                        codec.releaseOutputBuffer(outputBufIndex, false /* render */);

                        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            sawOutputEOS = true;
                        }
                    }
                    else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
                    {
                        codecOutputBuffers = codec.getOutputBuffers();
                    }
                    else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
                    {
                        final MediaFormat oformat = codec.getOutputFormat();
                        Log.d(TAG, "Output format has changed to " + oformat);
                        audioTrack.setPlaybackRate(oformat.getInteger(MediaFormat.KEY_SAMPLE_RATE));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private int getAndSelectAudioTrackIndex(MediaExtractor audioExtractor) {
        int numtracks = audioExtractor.getTrackCount();
        for (int i = 0; i < numtracks; i++) {
            audioFormat = audioExtractor.getTrackFormat(i);
            String mime = audioFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                return i;
            }
        }
        return -1;
    }
    public void requestStop() {
        mIsStopRequested = true;
    }
}
