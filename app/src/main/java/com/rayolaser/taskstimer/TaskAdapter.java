package com.rayolaser.taskstimer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import Entities.Task;

public class TaskAdapter extends ArrayAdapter<Task> {
    public TaskAdapter(Context context, List<Task> tasks) {
        super(context, 0, tasks);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Crear o reutilizar la vista
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        // Obtener la tarea
        Task task = getItem(position);

        // Configurar el texto para mostrar solo el nombre de la tarea
        TextView textView = convertView.findViewById(android.R.id.text1);
        assert task != null;
        String timeString = formatTime(task.getTime());
        String taskName;
        if (!Objects.equals(task.getTaskName(), "")) {
            taskName = task.getTaskName();
        }else {
            taskName = "-";
        }

        textView.setText(MessageFormat.format("{0}\n{1}", taskName, timeString));
        Log.d("TaskAdapter", "getView: " + task.getTime());

        return convertView;
    }

    public static String formatTime(long millis) {

        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = millis / (1000 * 60 * 60);

        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

}
