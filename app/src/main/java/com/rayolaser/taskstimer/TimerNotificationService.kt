package com.rayolaser.taskstimer

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class TimerNotificationService : Service() {
    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // Método para crear la notificación
    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(this, "timer_channel")
            .setContentTitle("Temporizador en ejecución")
            .setContentText("El temporizador está activo.")
            .setSmallIcon(R.drawable.timer_vector)
            .setOngoing(true) // Marca la notificación como persistente
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad alta para asegurar que se vea

        return builder.build()
    }
}
