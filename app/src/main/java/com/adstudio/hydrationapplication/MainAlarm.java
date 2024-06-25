package com.adstudio.hydrationapplication;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class MainAlarm {

    public static final int ALARM_REQUEST_CODE = 50;
    
    public static Intent intent;
    public static PendingIntent pendingIntent;
    public static AlarmManager manager;

    @SuppressLint("ScheduleExactAlarm")
    public static void setOnTimeAlarm(long nextTime, Context context) {
        intent = new Intent(context, MainReceiver.class);
        if (checkIfAlarmExists(context)) {
            cancelAlarm(context);
        }
        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent.putExtra("notification", "onTime");
        pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE);

        manager.setExact(AlarmManager.RTC, nextTime, pendingIntent);
    }

    @SuppressLint("ScheduleExactAlarm")
    public static void setExcessAlarm(long excessTime, Context context) {
        intent = new Intent(context, MainReceiver.class);
        if (checkIfAlarmExists(context)) {
            cancelAlarm(context);
        }
        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        intent.putExtra("notification", "excess");
        pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        manager.setExact(AlarmManager.RTC, excessTime, pendingIntent);
    }

    public static void cancelAlarm(Context context) {
        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (pendingIntent != null) {
            manager.cancel(pendingIntent);
        }
    }

    public static boolean checkIfAlarmExists(Context context) {
        return PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE,
                intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE) != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static boolean checkAlarmPermission(Context context) {
        manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        return manager.canScheduleExactAlarms();
    }
}
