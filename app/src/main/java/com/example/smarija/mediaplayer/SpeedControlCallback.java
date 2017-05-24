package com.example.smarija.mediaplayer;

import android.util.Log;

public class SpeedControlCallback implements com.example.smarija.mediaplayer.MoviePlayer.FrameCallback {
    private static final String TAG = "CAO";
    private static final boolean CHECK_SLEEP_TIME = false;

    private static final long ONE_MILLION = 1000000L;

    private long mPrevPresentUsec;
    public long mPrevMonoUsec;
    private long mFixedFrameDurationUsec;
    private boolean mLoopReset;
    long frameDelta;

    public void setFixedPlaybackRate(int fps) {
        if(fps==0)
            mFixedFrameDurationUsec=0;
        else
            mFixedFrameDurationUsec = fps;
    }

    // runs on decode thread
    @Override
    public void preRender(long presentationTimeUsec) {


        if (mPrevMonoUsec == 0) {
            mPrevMonoUsec = System.nanoTime() / 1000;
            mPrevPresentUsec = presentationTimeUsec;
        } else {

            if (mLoopReset) {
                mPrevPresentUsec = presentationTimeUsec - ONE_MILLION / 30;
                mLoopReset = false;
            }
            if (mFixedFrameDurationUsec != 0) {
                frameDelta = mFixedFrameDurationUsec;
            } else {
                frameDelta = presentationTimeUsec - mPrevPresentUsec;
            }
            if (frameDelta < 0) {
                Log.w(TAG, "Weird, video times went backward");
                frameDelta = 0;
            } else if (frameDelta == 0) {
                // This suggests a possible bug in movie generation.
                Log.i(TAG, "Warning: current frame and previous frame had same timestamp");
            } else if (frameDelta > 10 * ONE_MILLION) {
                Log.i(TAG, "Inter-frame pause was " + (frameDelta / ONE_MILLION) +
                        "sec, capping at 5 sec");
                frameDelta = 5 * ONE_MILLION;
            }

            long desiredUsec = mPrevMonoUsec + frameDelta;
            long nowUsec = System.nanoTime() / 1000;
            while (nowUsec < (desiredUsec - 100) /*&& mState == RUNNING*/) {
                long sleepTimeUsec = desiredUsec - nowUsec;
                if (sleepTimeUsec > 500000) {
                    sleepTimeUsec = 500000;
                }
                try {
                    if (CHECK_SLEEP_TIME) {
                        long startNsec = System.nanoTime();
                        Thread.sleep(sleepTimeUsec / 1000, (int) (sleepTimeUsec % 1000) * 1000);
                        long actualSleepNsec = System.nanoTime() - startNsec;
                        Log.d(TAG, "sleep=" + sleepTimeUsec + " actual=" + (actualSleepNsec/1000) +
                                " diff=" + (Math.abs(actualSleepNsec / 1000 - sleepTimeUsec)) +
                                " (usec)");
                    } else {
                        Thread.sleep(sleepTimeUsec / 1000, (int) (sleepTimeUsec % 1000) * 1000);
                    }
                } catch (InterruptedException ie) {}
                nowUsec = System.nanoTime() / 1000;
            }


            mPrevMonoUsec += frameDelta;
            mPrevPresentUsec += frameDelta;
        }
    }

    @Override public void postRender() {}

    @Override
    public void loopReset() {
        mLoopReset = true;
    }

    @Override
    public void resetTime() {
        mPrevMonoUsec = 0;
    }
}