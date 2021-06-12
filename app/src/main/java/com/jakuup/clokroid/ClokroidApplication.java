package com.jakuup.clokroid;

import android.app.Application;

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
