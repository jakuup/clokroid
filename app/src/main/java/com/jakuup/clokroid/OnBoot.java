package com.jakuup.clokroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBoot extends BroadcastReceiver {
    private static final String TAG = "OnBoot";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        ClokroidApplication.log(TAG, "onReceive, action is " + action);
        ClokroidApplication.kick(TAG + "#onReceive");

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            //Intent serviceIntent = new Intent(context, HomeNetworker.class);
            //context.startForegroundService(serviceIntent);
            //context.startService(serviceIntent);
            //setAlarm(context);
            Alarms.setNetworkAlarm(context, null);
        }
        //SharedPreferences preferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        //int counter = preferences.getInt("counter", 0);

        //SharedPreferences.Editor preferencesEditor = preferences.edit();
        //preferencesEditor.putInt("counter", counter + 1);
        //preferencesEditor.apply();

        //context.startService(new Intent(context, HomeNetworker.class));
    }
/*
    private void setAlarm(Context context) {
        ClockApplication.log(TAG, "setAlarm");

        //long dateNow = new Date().getTime();
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Alarms.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 10, pendingIntent);
        //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, dateNow, 1000 * 3, pendingIntent);
    }
*/
}
