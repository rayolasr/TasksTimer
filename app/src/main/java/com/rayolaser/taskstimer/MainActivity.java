package com.rayolaser.taskstimer;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import Entities.Task;

public class MainActivity extends AppCompatActivity {

    private final Handler handler = new Handler();
    private TextView timerTextView;
    private EditText taskNameEditText;
    private ListView taskListView;
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
            timerService.resetTimer();
            isRunning = false;
            timerTextView.setText(R.string._00_00_00);
        });

        saveButton.setOnClickListener(v -> {
            String taskName = taskNameEditText.getText().toString();
            if (!taskName.isEmpty()) {
                dbHelper.saveTask(taskName, updateTime);
                loadTasks();
                isRunning = false;
                timerService.resetTimer();
                timerTextView.setText(R.string._00_00_00);
                Toast.makeText(MainActivity.this, "Task saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Please enter a task name", Toast.LENGTH_SHORT).show();
            }
        });

        taskListView.setOnItemClickListener((parent, view, position, id) -> {
            Task selectedTask = (Task) parent.getItemAtPosition(position);
            int idTaskToDelete = selectedTask.getIdTask();

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Si el usuario confirma, elimina la tarea
                        dbHelper.deleteTask(String.valueOf(idTaskToDelete));
                        loadTasks(); // Recarga la lista de tareas
                        Toast.makeText(MainActivity.this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Cierra el diálogo si el usuario cancela
                        dialog.dismiss();
                    })
                    .show(); // Asegúrate de llamar a show() para mostrar el diálogo
        });
    }

    private void loadTasks() {
        List<Task> taskList = new ArrayList<>();
        Cursor cursor = dbHelper.getTasks();

        while (cursor.moveToNext()) {
            int idTaskIndex = cursor.getColumnIndex("id_task");
            int taskNameIndex = cursor.getColumnIndex("task_name");
            int timeIndex = cursor.getColumnIndex("id_task");
            int idTask = 0;
            String taskName = "Empty";
            long time = 0;
            if (idTaskIndex != -1) {
                idTask = cursor.getInt(idTaskIndex);
            }// Asegúrate de que tu cursor tenga la columna
            if (taskNameIndex != -1) {
                taskName = cursor.getString(taskNameIndex);
            }
            if (taskNameIndex != -1) {
                time = cursor.getLong(timeIndex);
            }

            Task task = new Task(idTask, taskName, time);
            taskList.add(task);
        }
        cursor.close();

        TaskAdapter adapter = new TaskAdapter(this, taskList);
        taskListView.setAdapter(adapter);
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