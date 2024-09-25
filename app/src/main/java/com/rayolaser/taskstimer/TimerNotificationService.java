package com.rayolaser.taskstimer;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class TimerNotificationService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        // Configura la notificación persistente aquí
        startForeground(1, createNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Lógica para el temporizador (si es necesario)
        return START_STICKY;  // El servicio seguirá funcionando incluso si la app es eliminada
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Detener cualquier lógica del temporizador (si es necesario)
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Método para crear la notificación
    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "timer_channel")
                .setContentTitle("Temporizador en ejecución")
                .setContentText("El temporizador está activo.")
                .setSmallIcon(R.drawable.timer_vector)
                .setOngoing(true) // Marca la notificación como persistente
                .setPriority(NotificationCompat.PRIORITY_HIGH);  // Prioridad alta para asegurar que se vea

        return builder.build();
    }
}
