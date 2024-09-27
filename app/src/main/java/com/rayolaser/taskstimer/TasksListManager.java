package com.rayolaser.taskstimer;

import static java.text.DateFormat.getDateInstance;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Entities.Task;

public class TasksListManager {
    private final TaskDatabaseHelper dbHelper;
    private final ListView taskListView;
    private final Context context;
    private final TextView currentDateTextView;

    public TasksListManager(Context context, ListView taskListView, TextView currentDateTextView) {
        dbHelper = new TaskDatabaseHelper(context);
        this.taskListView = taskListView;
        this.context = context;
        this.currentDateTextView = currentDateTextView;

        updateCurrentDate();

        taskListView.setOnItemClickListener((parent, view, position, id) -> {
            Task selectedTask = (Task) parent.getItemAtPosition(position);
            int idTaskToDelete = selectedTask.getIdTask();

            new AlertDialog.Builder(context)
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Si el usuario confirma, elimina la tarea
                        dbHelper.deleteTask(String.valueOf(idTaskToDelete));
                        loadTasks(); // Recarga la lista de tareas
                        Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Cierra el diálogo si el usuario cancela
                        dialog.dismiss();
                    })
                    .show(); // mostrar el diálogo
        });
    }

    public void loadTasks() {
        List<Task> taskList = new ArrayList<>();
        Cursor cursor = dbHelper.getTasks();

        while (cursor.moveToNext()) {
            int idTaskIndex = cursor.getColumnIndex("id_task");
            int taskNameIndex = cursor.getColumnIndex("task_name");
            int timeIndex = cursor.getColumnIndex("time");
            int dateIndex = cursor.getColumnIndex("task_date");
            int idTask = 0;
            String taskName = "Empty";
            String taskDate = "-";
            long time = 0;
            if (idTaskIndex != -1) {
                idTask = cursor.getInt(idTaskIndex);
            }
            if (taskNameIndex != -1) {
                taskName = cursor.getString(taskNameIndex);
            }
            if (taskNameIndex != -1) {
                time = cursor.getLong(timeIndex);
            }
            if (dateIndex != -1) {
                taskDate = cursor.getString(dateIndex);
            }

            Task task = new Task(idTask, taskName, time, taskDate);
            taskList.add(task);
        }
        cursor.close();

        TaskAdapter adapter = new TaskAdapter(context, taskList);
        taskListView.setAdapter(adapter);
    }

    // TODO: implement this way of formatting dates on the tasks list
    public void updateCurrentDate() {
        DateFormat dateFormat = getDateInstance();
        String currentDate = dateFormat.format(new Date());
        currentDateTextView.setText(currentDate);
    }
}
