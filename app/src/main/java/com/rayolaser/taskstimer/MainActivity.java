package com.rayolaser.taskstimer;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.annotation.SuppressLint;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView timerTextView;
    private EditText taskNameEditText;
    private ListView taskListView;
    private final Handler handler = new Handler();
    private long updateTime = 0L;
    private boolean isRunning = false;
    private TaskDatabaseHelper dbHelper;
    private TimerService timerService;
    private boolean isBound = false;

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
                timerTextView.setText(String.format("%02d:%02d:%02d", hours, mins, secs));
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
            timerTextView.setText(String.format("%02d:%02d:%02d", hours, mins, secs));

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

    @SuppressLint("SetTextI18n")
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
        taskListView = findViewById(R.id.task_list_view);

        loadTasks();

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
            timerService.startTimer();
            isRunning = false;
        });

        saveButton.setOnClickListener(v -> {
            String taskName = taskNameEditText.getText().toString();
            if (!taskName.isEmpty()) {
                dbHelper.saveTask(taskName, updateTime);
                Toast.makeText(MainActivity.this, "Task saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Please enter a task name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTasks() {
        ArrayList<String> taskList = new ArrayList<>();
        Cursor cursor = dbHelper.getTasks();

        if (cursor.moveToFirst()) {
            do {
                String taskName = cursor.getString(cursor.getColumnIndexOrThrow("task_name"));
                long time = cursor.getLong(cursor.getColumnIndexOrThrow("time"));
                taskList.add(taskName + ": " + formatTime(time));
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, taskList);
        taskListView.setAdapter(adapter);
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(long timeInMillis) {
        int secs = (int) (timeInMillis / 1000);
        int mins = secs / 60;
        int hours = mins / 60;
        secs = secs % 60;
        return String.format("%02d:%02d:%02d", hours, mins, secs);
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