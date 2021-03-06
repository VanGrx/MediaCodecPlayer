package com.example.mediacodec.mediaplayer;


import android.graphics.Color;
import android.widget.ProgressBar;


/**
 * Created by mediacodec on 26.4.2017.
 */

public class ProgressBarThread extends Thread {

    private ProgressBar pb_;
    private com.example.mediacodec.mediaplayer.MoviePlayer c_;

    private boolean mIsStopRequested=false;

    ProgressBarThread(ProgressBar pb, com.example.mediacodec.mediaplayer.MoviePlayer c) {
        pb_=pb;
        c_=c;

    }

    @Override
    public void run() {
        mIsStopRequested = false;
        long duration = c_.file_size;
        pb_.setMax((int)duration);
        pb_.setProgress(0);
        pb_.setDrawingCacheBackgroundColor(Color.RED);
        pb_.setVisibility(ProgressBar.VISIBLE);
        long total = c_.file_size;
        long currentPosition= 0;
        while (c_!=null && currentPosition<total && !mIsStopRequested) {
            try {
                Thread.sleep(100);

                currentPosition= (int)c_.current_size;
                if(currentPosition==-1)
                    currentPosition=duration;
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }
            pb_.setProgress((int)currentPosition);
        }
        if(mIsStopRequested) {
            pb_.setProgress(0);
        }
    }

    void requestStop() {
        mIsStopRequested = true;
    }

}
