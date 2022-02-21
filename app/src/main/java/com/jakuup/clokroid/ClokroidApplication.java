package com.jakuup.clokroid;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ClokroidApplication extends Application {
    private final static String TAG = "ClokroidApplication";

    private static ClokroidApplication mContext;
    private static Logger mLogger;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mLogger = new Logger();
        mLogger.write(TAG, "onCreate");
    }

    public static ClokroidApplication getContext() {
        return mContext;
    }

    public static void restart() {
        Intent startActivity = new Intent(mContext, MainActivity.class);
        int pendingIntentId = 22022022;
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, pendingIntentId, startActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);
        System.exit(0);
    }

    public static Logger getLogger() {
        return mLogger;
    }

    public static void log(String tag, String msg) {
        mLogger.write(tag, msg);
    }

    public static void kick(String tag) {
        mLogger.kick(tag);
    }
}
