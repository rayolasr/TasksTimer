package com.rayolaser.taskstimer;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class TimerService extends Service {

    private final Handler handler = new Handler();
    private long startTime;
    private boolean isRunning = false;
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
        startTime = SystemClock.elapsedRealtime();
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
}