package com.jakuup.clokroid;

import android.os.Handler;

import java.util.Calendar;
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

    public void scheduleAtFixedRate(int periodInSecs) {
        timer.scheduleAtFixedRate(timerTask, 0,  periodInSecs * 1000);
    }

    public void scheduleAtFullMinutes() {
        int currentSecs = Calendar.getInstance().get(Calendar.SECOND);
        int leftSecs = 60 - currentSecs;
        timer.scheduleAtFixedRate(timerTask, leftSecs * 1000, 60 * 1000);
    }

    public void cancel() {
        timerTask.cancel();
    }
}
