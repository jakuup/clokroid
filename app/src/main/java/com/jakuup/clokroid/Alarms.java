package com.jakuup.clokroid;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telecom.Call;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

public class Alarms extends BroadcastReceiver {
    private final static String TAG = "Alarms";

    private final static int ALARM_ID_NETWORK = 0;
//    private final static int ALARM_ID_CONTROLS_HIDER = 1;

    interface Callback extends Serializable {
        void onAlarm();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id", -1);
        Callback callback = (Callback)intent.getSerializableExtra("callback");

        Log.d(TAG, "#onReceive id=" + id);
        ClokroidApplication.kick(TAG + "#onReceive@" + id);
        if (callback != null) {
            Log.d(TAG, "#onReceive callback");
            callback.onAlarm();
        }
    }

    private static void setAlarm(Context context, int id, boolean repeated, long delay, Callback callback) {
        ClokroidApplication.log(TAG, "#setAlarm id=" + id + " repeated=" + repeated);

        Intent intent = new Intent(context, Alarms.class);
        intent.putExtra("id", id);
        intent.putExtra("callback", callback);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 1);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (repeated) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * delay, pendingIntent);
        }
        else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, 1000 * delay, pendingIntent);
        }
    }

    public static void setNetworkAlarm(Context context, Callback callback) {
        setAlarm(context, ALARM_ID_NETWORK, true, 10, callback);
    }
/*
    public static void setControlsHideAlarm(Context context, Callback callback) {
        setAlarm(context, ALARM_ID_CONTROLS_HIDER, false, 3, callback);
    }
 */
}
