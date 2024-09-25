package com.rayolaser.taskstimer;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private TextView timerTextView;
    private EditText taskNameEditText;
    private long updateTime = 0L;
    private TaskDatabaseHelper dbHelper;
    private TasksListManager tasksManager;
    private TimerManager timerManager;

    private final Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            if (timerManager.isRunning() || !timerManager.isPaused()) {
                updateTime = timerManager.getElapsedTime();
                int secs = (int) (updateTime / 1000);
                int mins = secs / 60;
                int hours = mins / 60;
                secs = secs % 60;
                timerTextView.setText(String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs));
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createNotificationChannel();

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        dbHelper = new TaskDatabaseHelper(this);

        timerTextView = findViewById(R.id.timer);
        taskNameEditText = findViewById(R.id.task_name);
        Button superButton = findViewById(R.id.super_button);
        ListView taskListView = findViewById(R.id.task_list_view);
        tasksManager = new TasksListManager(this, taskListView);
        timerManager = new TimerManager(this);

        handler.post(updateTimerThread);

        tasksManager.loadTasks();

        // Verificar si el servicio de notificación está corriendo
        if (!isServiceRunning()) {
            // Iniciar el servicio si no está corriendo
            Intent serviceIntent = new Intent(this, TimerNotificationService.class);
            startService(serviceIntent);
        }

        superButton.setOnClickListener(v -> {
            if (!timerManager.isRunning()) {
                Log.d("MainActivity", "Starting timer...");
                timerManager.start();
                startTimerNotification();
            } else {
                String taskName = taskNameEditText.getText().toString();
                if (!taskName.isEmpty()) {
                    dbHelper.saveTask(taskName, updateTime);
                    tasksManager.loadTasks();
                    timerManager.reset();
                    timerTextView.setText(R.string._00_00_00);
                    Toast.makeText(MainActivity.this, "Task saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a task name", Toast.LENGTH_SHORT).show();
                }
                stopTimerNotification();
            }
        });
    }

    private void startTimerNotification() {
        Intent serviceIntent = new Intent(this, TimerNotificationService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopTimerNotification() {
        Intent serviceIntent = new Intent(this, TimerNotificationService.class);
        stopService(serviceIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Timer Channel";
            String description = "Canal para el temporizador";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("timer_channel", name, importance);
            channel.setDescription(description);

            // Registrar el canal con el sistema
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TimerNotificationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}