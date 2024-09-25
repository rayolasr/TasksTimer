package com.rayolaser.taskstimer;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private TextView timerTextView;
    private EditText taskNameEditText;
    private long updateTime = 0L;
    private boolean isRunning = false;
    private TaskDatabaseHelper dbHelper;
    private TimerService timerService;
    private boolean isBound = false;
    private TasksManager tasksManager;

    private final Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            if (isRunning && isBound && timerService != null) {
                updateTime = timerService.getElapsedTime();
                int secs = (int) (updateTime / 1000);
                int mins = secs / 60;
                int hours = mins / 60;
                secs = secs % 60;
                //Log.d("TimerService", "Elapsed Time: " + updateTime);
                timerTextView.setText(String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs));
                handler.postDelayed(this, 1000);
            }
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("MainActivity", "Service Connected");
            // Vinculación exitosa, accedemos al servicio
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            timerService = binder.getService();
            isBound = true;
            handler.post(updateTimerThread);

            // Actualiza la UI con el tiempo transcurrido del servicio
            updateTime = timerService.getElapsedTime();
            int secs = (int) (updateTime / 1000);
            int mins = secs / 60;
            int hours = mins / 60;
            secs = secs % 60;
            //Log.d("MainActivity", "Elapsed Time: " + updateTime);
            timerTextView.setText(String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs));

            if (timerService.getIsRunning()) {
                isRunning = true;
                handler.post(updateTimerThread);// Empezamos a actualizar la interfaz con el tiempo del servicio
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        // Verificar si el servicio ya está corriendo
        if (!isServiceRunning()) {
            // Inicia el servicio si no está corriendo
            Intent serviceIntent = new Intent(this, TimerService.class);
            startService(serviceIntent);
        }

        // Vincular siempre el servicio
        Intent intent = new Intent(this, TimerService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        dbHelper = new TaskDatabaseHelper(this);

        timerTextView = findViewById(R.id.timer);
        taskNameEditText = findViewById(R.id.task_name);
        Button startButton = findViewById(R.id.start_button);
        Button pauseButton = findViewById(R.id.pause_button);
        Button resetButton = findViewById(R.id.reset_button);
        Button saveButton = findViewById(R.id.save_task_button);
        ListView taskListView = findViewById(R.id.task_list_view);
        tasksManager = new TasksManager(this, taskListView);

        tasksManager.loadTasks();

        startButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Starting timer...");
            Log.d("MainActivity", "isBound: " + isBound);
            Log.d("MainActivity", "timerService: " + timerService);
            if (isBound && timerService != null) {
                isRunning = true;
                timerService.startTimer();
                handler.post(updateTimerThread);
            }
        });

        pauseButton.setOnClickListener(v -> {
            timerService.pauseTimer();
            isRunning = false;
        });

        resetButton.setOnClickListener(v -> {
            timerService.resetTimer();
            isRunning = false;
            timerTextView.setText(R.string._00_00_00);
        });

        saveButton.setOnClickListener(v -> {
            String taskName = taskNameEditText.getText().toString();
            if (!taskName.isEmpty()) {
                dbHelper.saveTask(taskName, updateTime);
                tasksManager.loadTasks();
                isRunning = false;
                timerService.resetTimer();
                timerTextView.setText(R.string._00_00_00);
                Toast.makeText(MainActivity.this, "Task saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Please enter a task name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Desvincular el servicio cuando la actividad se destruya
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TimerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}