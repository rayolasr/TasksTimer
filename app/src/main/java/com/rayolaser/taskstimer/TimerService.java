package com.rayolaser.taskstimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import android.app.PendingIntent;


public class TimerService extends Service {

    private final Handler handler = new Handler();
    private boolean isRunning = false;
    private static final String CHANNEL_ID = "TimerServiceChannel";
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long updateTime = 0L;
    private long timeSwapBuff = 0L;
    private long elapsedTime = 0L;

    private final Runnable updateTimer = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime;
                //updateTime = timeSwapBuff + timeInMilliseconds;
                Log.d("TimerService", "Elapsed Time: " + elapsedTime);
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    public void onCreate() {
        Log.d("TimerService", "Service created.");

        // Intent para abrir MainActivity al tocar la notificación
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        );

        super.onCreate();
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Timer Service")
                .setContentText("The timer is running in the background.")
                .setSmallIcon(R.drawable.timer_vector)
                .setPriority(NotificationCompat.PRIORITY_LOW) // Prioridad baja para no molestar
                .setContentIntent(pendingIntent) // Añade el PendingIntent aquí
                .build();
        startForeground(1, notification);
        Log.d("TimerService", "Foreground service started with notification.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTimer);
        isRunning = false;

        // Eliminar la notificación cuando el servicio se detiene
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.cancel(1); // Cancela la notificación con ID 1
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
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

    public void startTimer() {
        if (!isRunning) {
            startTime = SystemClock.elapsedRealtime();  // Cambiado a elapsedRealtime
            isRunning = true;
            handler.post(updateTimer);
        }
    }

    public long getElapsedTime() {
        if (isRunning) {
            elapsedTime = SystemClock.elapsedRealtime() - startTime;  // Más preciso
        }
        return elapsedTime;
    }

    public void pauseTimer(){
        isRunning = false;
        handler.removeCallbacks(updateTimer);
    }

    public void resetTimer(){
        isRunning = false;
        handler.removeCallbacks(updateTimer);
        startTime = 0L;
        timeInMilliseconds = 0L;
        updateTime = 0L;
    }

    public boolean getIsRunning(){
        return isRunning;
    }
}