package com.jakuup.clokroid;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class HomeNetworker extends Service {
    private static final String TAG = "HomeNetworker";
    private final Handler handler = new Handler();
/*
    class HomeNetworkerBinder extends Binder {
        HomeNetworker getService() {
            return HomeNetworker.this;
        }
    }

    private final IBinder binder = new HomeNetworkerBinder();
*/
    @Override
    public IBinder onBind(Intent intent) {
/*
        ClockApplication.Log(TAG, "onBind");
        return binder;
 */
        return null;
    }

    @Override
    public void onCreate() {
        ClokroidApplication.log(TAG, "onCreate");
        super.onCreate();
    }
/*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ClockApplication.Log(TAG, "onStartCommand");
        RunTimerExecution();
        return super.onStartCommand(intent, flags, startId);
    }
*/
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        serviceTimer.cancel();
    }

    private Timer serviceTimer;

    private final Runnable run = () -> {
        Log.d(TAG, "run");
    };

    private void RunTimerExecution() {
        serviceTimer = new Timer();
        TimerTask serviceTimerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(run);
            }
        };

        serviceTimer.scheduleAtFixedRate(serviceTimerTask, 0, 1000);
    }

    public void Test() {
        Log.d(TAG, "Test");
    }

}

