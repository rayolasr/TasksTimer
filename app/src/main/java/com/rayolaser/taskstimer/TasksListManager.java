package com.rayolaser.taskstimer;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import Entities.Task;

public class TasksListManager {
    private final TaskDatabaseHelper dbHelper;
    private final ListView taskListView;
    private final Context context;

    public TasksListManager(Context context, ListView taskListView) {
        dbHelper = new TaskDatabaseHelper(context);
        this.taskListView = taskListView;
        this.context = context;

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
                    .show(); // Asegúrate de llamar a show() para mostrar el diálogo
        });
    }

    public void loadTasks() {
        List<Task> taskList = new ArrayList<>();
        Cursor cursor = dbHelper.getTasks();

        while (cursor.moveToNext()) {
            int idTaskIndex = cursor.getColumnIndex("id_task");
            int taskNameIndex = cursor.getColumnIndex("task_name");
            int timeIndex = cursor.getColumnIndex("time");
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

        TaskAdapter adapter = new TaskAdapter(context, taskList);
        taskListView.setAdapter(adapter);
    }
}
