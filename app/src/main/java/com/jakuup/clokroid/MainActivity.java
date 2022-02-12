package com.jakuup.clokroid;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements A2DPConnectorCallback {
    private static final String TAG = "MainActivity";

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler handler = new Handler();
    private boolean controlsVisible;

    private TimerScheduler clockUpdater;
    private final Runnable clockUpdaterTask = new Runnable() {
        @Override
        public void run() {
            String time = String.format("%1$tH:%1$tM", Calendar.getInstance());
            textClock.setText(time);
        }
    };

    private TimerScheduler autoBluetoothConnector;
    private final Runnable autoBluetoothConnectorTask = new Runnable() {
        @Override
        public void run() {
            connectA2DP();
        }
    };
    private boolean autoBluetoothConnect;
    private String autoBluetoothDevice;
    private int autoBluetoothInterval;

    private SharedPreferences preferences;

    private View viewBackground;
    private View viewControls;
    private TextView textClock;

    private final View.OnTouchListener onTouchListener = (view, motionEvent) -> {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (view == findViewById(R.id.buttonPreferences)) {
                    Intent settings = new Intent(this, SettingsActivity.class);
                    startActivity(settings);
                    stopHideTimer();
                    hide();
                }
                else if (view == findViewById(R.id.buttonExit)) {
                    finishAndRemoveTask();
                }
                else {
                    Log.i(TAG, view.toString());
                }
                break;
            case MotionEvent.ACTION_UP:
                view.performClick();
                break;
            default:
                break;
        }
        return false;
    };

    private A2DPConnector a2DPConnector;
    private boolean a2DPConnected = false;

    private void connectA2DP() {
        if (!a2DPConnected) {
            a2DPConnector.connect(autoBluetoothDevice);
        }
    }

    private void disconnectA2DP() {
        a2DPConnector.disconnect();
    }

    @Override
    public void notifyA2DPDevicesNames(String names) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("bth_paired_devices", names);
        editor.apply();
    }

    @Override
    public void notifyA2DPConnected(boolean connected) {
        Log.d(TAG, "device connected=" + connected);

        if (connected) {
            autoBluetoothConnector.cancel();
            autoBluetoothConnector = null;
        }
        a2DPConnected = connected;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        controlsVisible = true;
        viewBackground = findViewById(R.id.fullscreenBackground);
        viewControls = findViewById(R.id.fullscreenControls);
        textClock = findViewById(R.id.textClock);

        viewBackground.setOnClickListener(view -> toggle());
        viewControls.setOnClickListener(view -> toggle());
        textClock.setOnClickListener(view -> toggle());

        findViewById(R.id.buttonPreferences).setOnTouchListener(onTouchListener);
        findViewById(R.id.buttonExit).setOnTouchListener(onTouchListener);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        a2DPConnector = new A2DPConnector(this);
        a2DPConnector.setCallback(this);
        a2DPConnector.start();
        a2DPConnector.getBoundedDevices();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        hideDelayed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        TimerScheduler.pause();
        clockUpdater = null;
        autoBluetoothConnector = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        TimerScheduler.resume();

        clockUpdater = new TimerScheduler(clockUpdaterTask);
        clockUpdater.schedule(1);

        setupAutoBluetooth(preferences);
        if (!a2DPConnected && autoBluetoothConnect && !autoBluetoothDevice.isEmpty()) {
            autoBluetoothConnector = new TimerScheduler(autoBluetoothConnectorTask);
            autoBluetoothConnector.schedule(autoBluetoothInterval);
        }
    }

    private void setupAutoBluetooth(SharedPreferences sharedPreferences) {
        autoBluetoothConnect = sharedPreferences.getBoolean("bth_auto_connect", false);
        autoBluetoothDevice = sharedPreferences.getString("bth_auto_connect_device", "");
        autoBluetoothInterval = Integer.parseInt(preferences.getString("bth_auto_connect_interval", "60"));
    }

    private final Runnable doHide = () ->
              viewBackground.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                                                   | View.SYSTEM_UI_FLAG_FULLSCREEN
                                                   | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                                   | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                                   | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                                   | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    private final Runnable doShow = () -> viewControls.setVisibility(View.VISIBLE);

    private final Runnable runHide = this::hide;

    private void hide() {
        viewControls.setVisibility(View.GONE);
        controlsVisible = false;

        handler.removeCallbacks(doShow);
        handler.postDelayed(doHide, UI_ANIMATION_DELAY);
    }

    private void show() {
        controlsVisible = true;
        handler.removeCallbacks(doHide);
        handler.postDelayed(doShow, UI_ANIMATION_DELAY);
    }

    private void hideDelayed() {
        handler.removeCallbacks(runHide);
        handler.postDelayed(runHide, 1000);
    }

    private void toggle() {
        if (controlsVisible) {
            stopHideTimer();
            hide();
        } else {
            show();
            startAutoHide();
        }
    }

    private Timer autoHideTimer;

    private void startAutoHide() {
        autoHideTimer = new Timer();
        TimerTask autoHideTimerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(runHide);
            }
        };

        autoHideTimer.schedule(autoHideTimerTask, 5000);
    }

    private void stopHideTimer() {
        autoHideTimer.cancel();
        autoHideTimer.purge();
    }
}
