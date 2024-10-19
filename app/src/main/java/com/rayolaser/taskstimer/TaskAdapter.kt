package com.rayolaser.taskstimer

import com.rayolaser.taskstimer.entities.Task
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.MessageFormat
import java.util.Locale

class TaskAdapter(context: Context?, tasks: List<Task?>?) : ArrayAdapter<Task?>(
    context!!, 0, tasks!!
) {
    override fun getView(position: Int, convertViewParam: View?, parent: ViewGroup): View {
        // Crear o reutilizar la vista
        var convertView = convertViewParam
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
        }

        // Obtener la tarea
        val task = getItem(position)

        // Configurar el texto para mostrar solo el nombre de la tarea
        val textView = convertView!!.findViewById<TextView>(android.R.id.text1)
        checkNotNull(task)
        val timeString = formatTime(task.time)
        val taskName = if (task.taskName != "") {
            task.taskName
        } else {
            "-"
        }

        textView.text = MessageFormat.format("{0}\n{1}", taskName, timeString)
        Log.d("TaskAdapter", "getView: " + task.time)

        return convertView
    }

    companion object {
        fun formatTime(millis: Long): String {
            val seconds = (millis / 1000) % 60
            val minutes = (millis / (1000 * 60)) % 60
            val hours = millis / (1000 * 60 * 60)

            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}
