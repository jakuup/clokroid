package com.jakuup.clokroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

public class Logger implements ILogger {

    private static final String LOG_TXT = "log.txt";
    private static final String KICKS = "kicks";

    private final Context mContext;

    Logger() {
        mContext = ClokroidApplication.getContext();
    }

    private String timestamp() {
       return String.format(Locale.GERMAN, "%1$tY-%1$tm-%1$td %1$tH:%1$tM", Calendar.getInstance());
    }

    @Override
    public void clear() {
        mContext.deleteFile(LOG_TXT);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(KICKS, Context.MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
        preferencesEditor.clear();
        preferencesEditor.apply();
        Toast.makeText(mContext, mContext.getString(R.string.textLogsCleared), Toast.LENGTH_LONG).show();
    }

    @Override
    public String dump() {

        FileInputStream logFile;

        try {
            logFile = mContext.openFileInput(LOG_TXT);
        } catch (FileNotFoundException e) {
            return "";
        }

        InputStreamReader inputStreamReader = new InputStreamReader(logFile);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String receiveString;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            while ((receiveString = bufferedReader.readLine()) != null) {
                stringBuilder.append(receiveString + "\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        try {
            logFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        stringBuilder.append("\nKICKS {\n");
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(KICKS, Context.MODE_PRIVATE);
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            stringBuilder.append("  " + entry.getKey() + ":" + entry.getValue().toString() + "\n");
        }
        stringBuilder.append("\n}\n");
        return stringBuilder.toString();
    }

    @Override
    public void write(String tag, String msg) {
        Log.d(tag, msg);

        FileOutputStream logFile;

        try {
            logFile = mContext.openFileOutput(LOG_TXT, Context.MODE_APPEND);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        String buffer = "[" + timestamp() + "][" + tag + "] " + msg + "\n";

        try {
            logFile.write(buffer.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            logFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void kick(String tag) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(KICKS, Context.MODE_PRIVATE);

        long kicks = sharedPreferences.getLong(tag, 0);

        SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
        preferencesEditor.putLong(tag, kicks + 1);
        preferencesEditor.putString(tag + "@last", timestamp());
        preferencesEditor.apply();
    }

}
