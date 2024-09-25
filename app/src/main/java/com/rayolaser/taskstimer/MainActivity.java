package com.rayolaser.taskstimer;

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
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        dbHelper = new TaskDatabaseHelper(this);

        timerTextView = findViewById(R.id.timer);
        taskNameEditText = findViewById(R.id.task_name);
        Button startButton = findViewById(R.id.start_button);
        Button pauseButton = findViewById(R.id.pause_button);
        Button resetButton = findViewById(R.id.reset_button);
        Button saveButton = findViewById(R.id.save_task_button);
        ListView taskListView = findViewById(R.id.task_list_view);
        tasksManager = new TasksListManager(this, taskListView);
        timerManager = new TimerManager(this);

        handler.post(updateTimerThread);

        tasksManager.loadTasks();

        startButton.setOnClickListener(v -> {
            Log.d("MainActivity", "Starting timer...");
            timerManager.start();
        });

        pauseButton.setOnClickListener(v -> timerManager.pause());

        resetButton.setOnClickListener(v -> {
            timerManager.reset();
            timerTextView.setText(R.string._00_00_00);
        });

        saveButton.setOnClickListener(v -> {
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
        });
    }
}