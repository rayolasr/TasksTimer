package com.rayolaser.timer;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView timerTextView;
    private EditText taskNameEditText;
    private ListView taskListView;
    private final Handler handler = new Handler();
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updateTime = 0L;
    private boolean isRunning = false;
    private TaskDatabaseHelper dbHelper;

    private final Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                timeInMilliseconds = System.currentTimeMillis() - startTime;
                updateTime = timeSwapBuff + timeInMilliseconds;
                int secs = (int) (updateTime / 1000);
                int mins = secs / 60;
                int hours = mins / 60;
                secs = secs % 60;
                timerTextView.setText(String.format("%02d:%02d:%02d", hours, mins, secs));
                handler.postDelayed(this, 0);
            }
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            if (!isRunning) {
                startTime = System.currentTimeMillis();
                handler.postDelayed(updateTimerThread, 0);
                isRunning = true;
            }
        });

        pauseButton.setOnClickListener(v -> {
            if (isRunning) {
                timeSwapBuff += timeInMilliseconds;
                handler.removeCallbacks(updateTimerThread);
                isRunning = false;
            }
        });

        resetButton.setOnClickListener(v -> {
            startTime = 0L;
            timeSwapBuff = 0L;
            timeInMilliseconds = 0L;
            timerTextView.setText("00:00:00");
            isRunning = false;
            handler.removeCallbacks(updateTimerThread);
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
        saveButton.setOnClickListener(v -> {
            String taskName = taskNameEditText.getText().toString();
            if (!taskName.isEmpty()) {
                dbHelper.saveTask(taskName, updateTime);
                Toast.makeText(MainActivity.this, "Task saved", Toast.LENGTH_SHORT).show();
                loadTasks();
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
}