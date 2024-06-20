package com.adstudio.hydrationapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.TimeUnit;

public class MainReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentType = intent.getStringExtra("notification");
        switch (intentType) {
            case "update":
                MainForegroundService.updateNotification(context);
                break;
            case "alarm":
                MainForegroundService.onTimeWater(context);
                break;
            case "excess":
                LocalAppData.excessTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30);
                MainForegroundService.onTimeWater(context);
                break;
            case "stop":
                Intent it = new Intent(context, MainForegroundService.class);
                MainForegroundService.stopService(context, it);
        }
    }
}