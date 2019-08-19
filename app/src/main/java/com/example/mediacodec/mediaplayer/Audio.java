package com.example.mediacodec.mediaplayer;

import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;



public class Audio extends Thread{

    String TAG = "Player";

    long rewind_timer = -1;

    public MediaFormat audioFormat;
    public AudioTrack audioTrack;
    MediaExtractor extractor;

    boolean stop = false;
    public File fis;

    public boolean paused = false;
    public boolean fastforward = false;
    public boolean rewind = false;
    public long current_size = 0;

    File sampleFD;
    private boolean mIsStopRequested;
    public AssetFileDescriptor msampleFD;
    public boolean useDescriptop;
    public  MediaFormat oformat ;

    public void exit(){
        stop = true;
    }

    @Override
    public void run() {

        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
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
            if(useDescriptop){
                extractor.setDataSource(msampleFD.getFileDescriptor(), msampleFD.getStartOffset(), msampleFD.getLength());
            }
            else {
                extractor.setDataSource(String.valueOf(sampleFD));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int track_index = getAndSelectAudioTrackIndex(extractor);
        MediaFormat format = extractor.getTrackFormat(track_index);
        String mime = format.getString(MediaFormat.KEY_MIME);

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
                if(fastforward || rewind){
                    audioTrack.pause();
                   continue;
                }
                else
                    rewind_timer=-1;
                if (mIsStopRequested) {
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
                    codec.queueInputBuffer(inputBufIndex, 0, sampleSize,presentationTimeUs,
                            sawInputEOS ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0);
                    if (!sawInputEOS) {
                        extractor.advance();
                        current_size = extractor.getSampleTime();
                    }
                    BufferInfo info = new BufferInfo();
                    final int res = codec.dequeueOutputBuffer(info, 1000);
                    if (res >= 0) {
                        ByteBuffer buf = codecOutputBuffers[res];
                        final byte[] chunk = new byte[info.size];
                        buf.get(chunk); // Read the buffer all at once
                        buf.clear(); // ** MUST DO!!! OTHERWISE THE NEXT TIME YOU GET THIS SAME BUFFER BAD THINGS WILL HAPPEN
                        audioTrack.play(); if (chunk.length > 0)
                            audioTrack.write(chunk, 0, chunk.length);
                        codec.releaseOutputBuffer(res, false /* render */);
                         if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0)
                            sawOutputEOS = true;
                    }

                    else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED)
                    {
                        codecOutputBuffers = codec.getOutputBuffers();
                    }
                    else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED)
                    {
                        oformat = codec.getOutputFormat();
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
}
