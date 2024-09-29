package com.rayolaser.taskstimer

import Entities.Task
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

class TasksListManager(
    private val context: Context,
    private val taskListView: ListView,
    private val currentDateTextView: TextView
) {
    private val dbHelper = TaskDatabaseHelper(context)
    private var listDate: Date = Date()

    init {
        //val currentDate = Date()
        listDate = Date()
        updateDateTextView(listDate)

        taskListView.onItemClickListener =
            OnItemClickListener { parent: AdapterView<*>, _: View?, position: Int, _: Long ->
                val selectedTask = parent.getItemAtPosition(position) as Task
                val idTaskToDelete = selectedTask.idTask

                AlertDialog.Builder(context)
                    .setTitle("Eliminar tarea")
                    .setMessage("¿Estás seguro de que deseas eliminar esta tarea?")
                    .setPositiveButton("Sí") { _: DialogInterface?, _: Int ->
                        // Si el usuario confirma, elimina la tarea
                        dbHelper.deleteTask(idTaskToDelete.toString())
                        loadTasks(listDate) // Recarga la lista de tareas
                        Toast.makeText(context, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No") { dialog: DialogInterface, _: Int ->
                        // Cierra el diálogo si el usuario cancela
                        dialog.dismiss()
                    }
                    .show() // mostrar el diálogo
            }
    }

    fun loadTasks(date: Date? = Date()) {
        val taskList: MutableList<Task> = ArrayList()
        Log.d("TasksListManager", "currentDate: $date")
        val cursor = dbHelper.getTasks(date)

        while (cursor.moveToNext()) {
            val idTaskIndex = cursor.getColumnIndex("id_task")
            val taskNameIndex = cursor.getColumnIndex("task_name")
            val timeIndex = cursor.getColumnIndex("time")
            val dateIndex = cursor.getColumnIndex("task_date")
            var idTask = 0
            var taskName: String? = "Empty"
            var taskDate: String? = "-"
            var time: Long = 0
            if (idTaskIndex != -1) {
                idTask = cursor.getInt(idTaskIndex)
            }
            if (taskNameIndex != -1) {
                taskName = cursor.getString(taskNameIndex)
            }
            if (taskNameIndex != -1) {
                time = cursor.getLong(timeIndex)
            }
            if (dateIndex != -1) {
                taskDate = cursor.getString(dateIndex)
            }

            val task = Task(idTask, taskName, time, taskDate)
            taskList.add(task)
        }
        cursor.close()

        val adapter = TaskAdapter(context, taskList)
        taskListView.adapter = adapter
    }

    // TODO: implement this way of formatting dates on the tasks list
    private fun updateDateTextView(date: Date) {
        val dateFormat = DateFormat.getDateInstance()
        val currentDate = dateFormat.format(date)
        currentDateTextView.text = currentDate
    }

    fun previousDate() {
        val calendar = Calendar.getInstance()
        calendar.time = listDate
        calendar.add(Calendar.DAY_OF_YEAR, -1) // Restamos 1 día
        listDate = calendar.time
        val previousDay = listDate
        updateDateTextView(previousDay)
        loadTasks(previousDay)
    }

    fun nextDate() {
        val calendar = Calendar.getInstance()
        calendar.time = listDate
        calendar.add(Calendar.DAY_OF_YEAR, +1) // Restamos 1 día
        listDate = calendar.time
        val nextDate = listDate
        updateDateTextView(nextDate)
        loadTasks(nextDate)
    }
}
