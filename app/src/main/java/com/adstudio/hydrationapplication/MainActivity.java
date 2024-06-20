package com.adstudio.hydrationapplication;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    private static final int NOTIFICATION_REQUEST_CODE = 20;
    private static final String NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        LocalAppData.loadData(MainActivity.this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionDialog();
        }
        else {
            showStartupAlert();
        }

        Button stopService = (Button) findViewById(R.id.stopService);
        stopService.setBackgroundColor(Color.parseColor("#449AFF"));
        stopService.setTextColor(Color.WHITE);
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!LocalAppData.serviceRunning(MainActivity.this)) {
                    Toast.makeText(MainActivity.this,"The service isn't running", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent it = new Intent(MainActivity.this, MainForegroundService.class);
                    MainForegroundService.stopService(MainActivity.this, it);
                }
            }
        });

        LottieAnimationView waterCup = (LottieAnimationView) findViewById(R.id.cup);
        waterCup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    aboveVersionS();
                }
                else if (!LocalAppData.serviceRunning(MainActivity.this)) {
                    Intent it = new Intent(MainActivity.this, MainForegroundService.class);
                    startService(it);
                }
                else MainForegroundService.updateNotification(MainActivity.this);

                if (!LocalAppData.firstTimeInit) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("First Time Tutorial:");
                    builder.setMessage("Now that you have initialized the service, there is a " +
                            "notification that appeared telling you the next time you should drink " +
                            "water. If you drink earlier than the time says, simply press the \"I Drank!\" " +
                            "button in the notification or tap the water cup in the app!");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Okay!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LocalAppData.firstTimeInit = true;
                            LocalAppData.saveData(MainActivity.this);
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void aboveVersionS() {
        if (!MainAlarm.checkAlarmPermission(MainActivity.this) || !checkNotificationPermission()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Permissions Denied");
            builder.setMessage("It seems that there are permissions that haven't " +
                    "been granted. Open your settings app and allow these permissions " +
                    "so this app can function properly");
            builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent alarmIntent = new Intent();
                    alarmIntent.setAction(Settings.ACTION_APPLICATION_SETTINGS);
                    startActivity(alarmIntent);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();
        }
        else if (!LocalAppData.serviceRunning(MainActivity.this)) {
            Intent it = new Intent(MainActivity.this, MainForegroundService.class);
            ContextCompat.startForegroundService(MainActivity.this, it);
        }
        else {
            MainForegroundService.updateNotification(MainActivity.this);
        }
    }

    public void showStartupAlert() {
        if (!LocalAppData.accepted) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Welcome to PureHydrate!");
            builder.setMessage("This app's intent is to remind the user when to drink water through " +
                    "an interactive UI that is both relaxing and simple. To get started, simply " +
                    "tap the water cup icon to initialize the service!");
            builder.setCancelable(false);
            builder.setPositiveButton("I Understand", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    LocalAppData.accepted = true;
                    LocalAppData.saveData(MainActivity.this);
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    public boolean checkNotificationPermission() {
        return ContextCompat.checkSelfPermission
                (MainActivity.this, NOTIFICATION_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public void notificationPermissionDialog() {
        if (!checkNotificationPermission()) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{NOTIFICATION_PERMISSION},
                    NOTIFICATION_REQUEST_CODE);
        }
        else if (checkNotificationPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmPermissionDialog();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == NOTIFICATION_REQUEST_CODE) {
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (MainActivity.this, NOTIFICATION_PERMISSION)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Permission Denied");
                builder.setMessage("This application requires the use of notifications in order to " +
                        "function properly. We request you grant the permission " +
                        "in order for the app to work. If you decline now and wish to " +
                        "grant the permission later, simply open the application's settings " +
                        "and grant the required permissions.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        notificationPermissionDialog();
                        dialog.dismiss();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
            else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(MainActivity.this,
                        "Notification Permission denied. Open your settings and grant the " +
                                "permission",
                        Toast.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmPermissionDialog();
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    alarmPermissionDialog();
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void alarmPermissionDialog() {
        if (!MainAlarm.checkAlarmPermission(MainActivity.this)) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Alarm Permission Required");
            builder.setMessage("This app requires the use of alarms in order to function properly." +
                    "Please grant the permission in the setting when prompted to continue.");
            builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent alarmIntent = new Intent();
                    alarmIntent.setAction(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    MainActivity.this.startActivity(alarmIntent);
                    showStartupAlert();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(
                            MainActivity.this,
                            "Alarm Permissions denied. Open your settings and grant " +
                                    "the permissions",
                            Toast.LENGTH_LONG
                    ).show();
                    dialog.cancel();
                    showStartupAlert();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }
}
