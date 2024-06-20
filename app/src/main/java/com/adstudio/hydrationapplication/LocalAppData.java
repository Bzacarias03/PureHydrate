package com.adstudio.hydrationapplication;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class LocalAppData extends AppCompatActivity {

    private final static String SHARED_PREFS = "Shared_Prefs";

    private final static String ACCEPTED = "Accepted";
    private final static String NEXT_TIME = "Next_Time";
    private final static String FIRST_TIME_INIT = "First_Time_Init";

    public static boolean accepted;
    public static boolean firstTimeInit;
    public static long excessTime;

    public static void saveData(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(ACCEPTED, accepted);
        editor.putBoolean(FIRST_TIME_INIT, firstTimeInit);
        editor.putLong(NEXT_TIME, excessTime);
        editor.apply();
    }

    public static void loadData(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        accepted = preferences.getBoolean(ACCEPTED, false);
        firstTimeInit = preferences.getBoolean(FIRST_TIME_INIT, false);
        excessTime = preferences.getLong(NEXT_TIME, 0);
    }

    public static boolean serviceRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (MainForegroundService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
