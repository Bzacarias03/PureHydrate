package com.adstudio.hydrationapplication;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainForegroundService extends Service {

    private static NotificationManager notificationManager;
    private static PendingIntent activityIntent;
    private static PendingIntent receiverIntent;
    private static PendingIntent stopIntent;
    private static Date date;
    private Context context;

    private static final String CHANNELID = "ChannelID";
    private static final int FOREGROUND_NOTIFICATION_ID = 2;
    private static final int ACTIVITY_ID = 1;
    private static final int UPDATE_NOTIF_ID = 5;
    private static final int STOP_SERVICE_ID = 10;
    public static final int NEXT_DRINK = 2;
    public static final int EXCESS_TIME = 8;
    public static Random random = new Random();

    public static String[] messages = {
            "Take a break from what you're doing and get some water!",
            "Working hard is important, but so is a cup of water. Have a drink!",
            "You slip up when you don't drink up >:( DRINK SOME WATER!!",
            "Grabbing a cup of water is a great way to take care of yourself. Get a Drink!",
            "Water break! It's like hitting the reset button for the day.",
            "Water is nature's energy drink. Make sure to fuel up.",
            "Damn bro you looking parched. Drink some water or something.",
            "GLUG GLUG GLUG GLUG GLUG, that should be you with some water.",
            "Don't be a shriveled raisin. Be a juicy grape! Drink some water.",
            "Ayo, don't forget to hydrate fam."};

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        activityIntent = createActivityIntent(context);
        receiverIntent = createReceiverIntent(context);
        stopIntent = stopServiceIntent(context);

        date = new Date(System.currentTimeMillis());
        date.setTime(date.getTime() + TimeUnit.HOURS.toMillis(NEXT_DRINK));
        LocalAppData.excessTime = date.getTime() + TimeUnit.HOURS.toMillis(EXCESS_TIME);
        LocalAppData.saveData(context);

        startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification(date.getTime(), context));
        return super.onStartCommand(intent, flags, startId);
    }

    private static Notification buildNotification(long time, Context context) {
        MainAlarm.setOnTimeAlarm(time, context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNELID,
                    "Channel ID",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("This channel is meant to hold the foreground service");
            notificationManager.createNotificationChannel(channel);
        }

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, CHANNELID);
        }
        else {
            builder = new Notification.Builder(context);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher);

        String nextTime = new SimpleDateFormat("h:mm a").format(date);
        builder.setContentText("Time till next drink: " + nextTime);
        builder.setContentIntent(activityIntent);
        builder.addAction(new Notification.Action.Builder(
                R.drawable.ic_launcher_background,
                "I Drank!",
                receiverIntent
        ).build());
        builder.addAction(new Notification.Action.Builder(
                R.drawable.ic_launcher_background,
                "Stop Service!",
                stopIntent
        ).build());
        builder.setShowWhen(false);
        builder.setAutoCancel(true);
        builder.setOngoing(false);

        return builder.build();
    }

    public static void updateNotification(Context context) {
        date.setTime(date.getTime() + TimeUnit.HOURS.toMillis(NEXT_DRINK));
        if(date.getTime() >= LocalAppData.excessTime) {
            excessWater(context);
            return;
        }
        Notification updated = buildNotification(date.getTime(), context);
        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, updated);
    }

    public static void excessWater(Context context) {
        Notification.Builder excessNotification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            excessNotification = new Notification.Builder(context, CHANNELID);
        }
        else {
            excessNotification = new Notification.Builder(context);
        }
        excessNotification.setContentTitle("Slow down there man!!");
        excessNotification.setContentText("You've drank enough water for now. Take some time to relax" +
                " and we'll make sure to remind you later.");
        excessNotification.setSmallIcon(R.mipmap.ic_launcher);
        excessNotification.setPriority(Notification.PRIORITY_HIGH);
        excessNotification.setContentIntent(activityIntent);
        excessNotification.addAction(new Notification.Action.Builder(
                R.drawable.ic_launcher_background,
                "Stop Service!",
                stopIntent
        ).build());
        excessNotification.setShowWhen(false);
        excessNotification.setAutoCancel(true);
        excessNotification.setOngoing(false);

        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, excessNotification.build());
        MainAlarm.setExcessAlarm(LocalAppData.excessTime, context);
    }

    public static void onTimeWater(Context context) {
        Notification.Builder onTimeNotification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            onTimeNotification = new Notification.Builder(context, CHANNELID);
        }
        else {
            onTimeNotification = new Notification.Builder(context);
        }
        onTimeNotification.setContentTitle("Time to drink!!");
        int i = random.nextInt(messages.length);
        String message = messages[i];

        onTimeNotification.setContentText(message);
        onTimeNotification.setSmallIcon(R.mipmap.ic_launcher);
        onTimeNotification.setPriority(Notification.PRIORITY_HIGH);
        onTimeNotification.setContentIntent(activityIntent);
        onTimeNotification.addAction(new Notification.Action.Builder(
                R.drawable.ic_launcher_background,
                "I Drank!",
                receiverIntent
        ).build());
        onTimeNotification.addAction(new Notification.Action.Builder(
                R.drawable.ic_launcher_background,
                "Stop Service!",
                stopIntent
        ).build());
        onTimeNotification.setShowWhen(false);
        onTimeNotification.setAutoCancel(true);
        onTimeNotification.setOngoing(false);

        notificationManager.notify(FOREGROUND_NOTIFICATION_ID, onTimeNotification.build());
    }

    public static PendingIntent createActivityIntent(Context context) {
        Intent it = new Intent(context, MainActivity.class);
        it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIt = PendingIntent.getActivity(
                context,
                ACTIVITY_ID,
                it,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        return pendingIt;
    }

    public static PendingIntent createReceiverIntent(Context context) {
        Intent it2 = new Intent(context, MainReceiver.class);
        it2.putExtra("notification", "update");
        PendingIntent pendingIt2 = PendingIntent.getBroadcast(
                context,
                UPDATE_NOTIF_ID,
                it2,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        return pendingIt2;
    }

    public static PendingIntent stopServiceIntent(Context context) {
        Intent it3 = new Intent(context, MainReceiver.class);
        it3.putExtra("notification", "stop");
        PendingIntent pendingit3 = PendingIntent.getBroadcast(
                context,
                STOP_SERVICE_ID,
                it3,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
        return pendingit3;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void stopService(Context context, Intent intent) {
        context.stopService(intent);
        MainAlarm.cancelAlarm(context);
        Toast.makeText(context,"Service stopped", Toast.LENGTH_SHORT).show();
    }
}
