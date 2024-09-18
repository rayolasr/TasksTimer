package com.rayolaser.taskstimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import androidx.core.app.NotificationCompat;


public class TimerService extends Service {

    private final Handler handler = new Handler();
    private long startTime;
    private boolean isRunning = false;
    private static final String CHANNEL_ID = "TimerServiceChannel";

    private final Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                long elapsedTime = SystemClock.elapsedRealtime() - startTime;
                handler.postDelayed(this, 1000);
                Log.d("TimerService", "Elapsed time: " + elapsedTime);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Timer Service")
                .setContentText("The timer is running in the background.")
                .setSmallIcon(R.drawable.timer_vector)
                .setPriority(NotificationCompat.PRIORITY_LOW) // Prioridad baja para no molestar
                .build();
        startForeground(1, notification);
        Log.d("TimerService", "Foreground service started with notification.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            startTime = SystemClock.elapsedRealtime();
            handler.post(updateTimer);
            isRunning = true;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimer);
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.setDescription("Channel for Timer Service");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
                Log.d("TimerService", "Notification channel created.");
            } else {
                Log.e("TimerService", "NotificationManager is null.");
            }
        }
    }
}