package com.rayolaser.taskstimer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
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
                String timeString = formatTime(updateTime);
                timerTextView.setText(timeString);
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
        ImageButton superButton = findViewById(R.id.super_button);
        ListView taskListView = findViewById(R.id.task_list_view);
        TextView currentDateTextView = findViewById(R.id.currentDateTextView);
        TextView buttonText = findViewById(R.id.super_button_text);
        tasksManager = new TasksListManager(this, taskListView, currentDateTextView);
        timerManager = new TimerManager(this);

        handler.post(updateTimerThread);

        tasksManager.loadTasks();

        // Verificar si el servicio de notificación está corriendo
        if (!isServiceRunning()) {
            // Iniciar el servicio si no está corriendo
            Intent serviceIntent = new Intent(this, TimerNotificationService.class);
            startService(serviceIntent);
        }

        if (timerManager.isRunning()) {
            buttonText.setText(R.string.stop_and_save);
            superButton.setImageResource(R.drawable.pause_vector);
        }

        // TODO: move all timer button logic to TimerManager
        superButton.setOnClickListener(v -> {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(superButton, "alpha", 1f, 0f);
            fadeOut.setDuration(300);

            if (!timerManager.isRunning()) {
                Log.d("MainActivity", "Starting timer...");
                timerManager.start();
                startTimerNotification();
            } else {
                String taskName = taskNameEditText.getText().toString();
                updateTime = timerManager.getElapsedTime();
                dbHelper.saveTask(taskName, updateTime);
                tasksManager.loadTasks();
                timerManager.reset();
                timerTextView.setText(R.string._00_00_00);
                Toast.makeText(MainActivity.this, "Task saved", Toast.LENGTH_SHORT).show();
                taskNameEditText.setText("");
                stopTimerNotification();
            }

            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (timerManager.isRunning()) {
                        // Cambiar a estado de detenido
                        buttonText.setText(R.string.stop_and_save);
                        superButton.setImageResource(R.drawable.pause_vector);
                    } else {
                        // Cambiar a estado de en ejecución
                        buttonText.setText(R.string.start);
                        superButton.setImageResource(R.drawable.play_vector);
                    }

                    // Animación de desvanecimiento para que vuelva a aparecer
                    ObjectAnimator fadeInImage = ObjectAnimator.ofFloat(superButton, "alpha", 0f, 1f);
                    ObjectAnimator fadeInText = ObjectAnimator.ofFloat(buttonText, "alpha", 0f, 1f);
                    fadeInImage.setDuration(300);
                    fadeInText.setDuration(300);
                    fadeInImage.start();
                    fadeInText.start();
                }
            });
            fadeOut.start();
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

    public static String formatTime(long millis) {

        long seconds = (millis / 1000) % 60;  // Convertir a segundos y obtener el residuo de 60
        long minutes = (millis / (1000 * 60)) % 60;  // Convertir a minutos y obtener el residuo de 60
        long hours = millis / (1000 * 60 * 60);  // Convertir a horas

        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds); // Formato HH:MM:SS
    }
}