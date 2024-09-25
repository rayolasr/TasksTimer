package com.rayolaser.taskstimer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

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

        assert task != null;
        int secs = (int) task.getTime();
        int mins = secs / 60;
        int hours = mins / 60;

        // Configurar el texto para mostrar solo el nombre de la tarea
        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(task.getTaskName() + ": " + hours + ":" + mins + ":" + secs);

        return convertView;
    }
}
