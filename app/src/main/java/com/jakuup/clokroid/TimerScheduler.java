package com.jakuup.clokroid;

import android.os.Handler;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;


public class TimerScheduler {

    private static Timer timer = null;
    private static final Handler handler = new Handler();
    private final TimerTask timerTask;

    public TimerScheduler(Runnable callback) {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(callback);
            }
        };
    }

    public static void pause() {
        timer.cancel();
    }

    public static void resume() {
        timer = new Timer();
    }

    public void schedule(int periodInSecs) {
        timer.scheduleAtFixedRate(timerTask, 0, periodInSecs * 1000);
    }

}
