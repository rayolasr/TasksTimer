package com.rayolaser.taskstimer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val handler = Handler()
    private lateinit var timerTextView: TextView
    private lateinit var taskNameEditText: EditText
    private var updateTime = 0L
    private var dbHelper: TaskDatabaseHelper? = null
    private var tasksManager: TasksListManager? = null
    private var timerManager: TimerManager? = null

    private val updateTimerThread: Runnable = object : Runnable {
        override fun run() {
            if (timerManager!!.isRunning || !timerManager!!.isPaused) {
                updateTime = timerManager!!.elapsedTime
                val timeString = formatTime(updateTime)
                timerTextView!!.text = timeString
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContentView(R.layout.activity_main)

        dbHelper = TaskDatabaseHelper(this)

        timerTextView = findViewById(R.id.timer)
        taskNameEditText = findViewById(R.id.task_name)
        val superButton = findViewById<ImageButton>(R.id.super_button)
        val taskListView = findViewById<ListView>(R.id.task_list_view)
        val currentDateTextView = findViewById<TextView>(R.id.currentDateTextView)
        val buttonText = findViewById<TextView>(R.id.super_button_text)
        tasksManager = TasksListManager(this, taskListView, currentDateTextView)
        timerManager = TimerManager(this)

        handler.post(updateTimerThread)

        tasksManager!!.loadTasks()

        // Verificar si el servicio de notificación está corriendo
        if (!isServiceRunning) {
            // Iniciar el servicio si no está corriendo
            val serviceIntent = Intent(this, TimerNotificationService::class.java)
            startService(serviceIntent)
        }

        if (timerManager!!.isRunning) {
            buttonText.setText(R.string.stop_and_save)
            superButton.setImageResource(R.drawable.pause_vector)
        }

        // TODO: move all timer button logic to TimerManager
        superButton.setOnClickListener { v: View? ->
            val fadeOut = ObjectAnimator.ofFloat(superButton, "alpha", 1f, 0f)
            fadeOut.setDuration(300)

            if (!timerManager!!.isRunning) {
                Log.d("MainActivity", "Starting timer...")
                timerManager!!.start()
                startTimerNotification()
            } else {
                val taskName = taskNameEditText.getText().toString()
                updateTime = timerManager!!.elapsedTime
                dbHelper!!.saveTask(taskName, updateTime)
                tasksManager!!.loadTasks()
                timerManager!!.reset()
                timerTextView.setText(R.string._00_00_00)
                Toast.makeText(this@MainActivity, "Task saved", Toast.LENGTH_SHORT).show()
                taskNameEditText.setText("")
                stopTimerNotification()
            }

            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (timerManager!!.isRunning) {
                        // Cambiar a estado de detenido
                        buttonText.setText(R.string.stop_and_save)
                        superButton.setImageResource(R.drawable.pause_vector)
                    } else {
                        // Cambiar a estado de en ejecución
                        buttonText.setText(R.string.start)
                        superButton.setImageResource(R.drawable.play_vector)
                    }

                    // Animación de desvanecimiento para que vuelva a aparecer
                    val fadeInImage = ObjectAnimator.ofFloat(superButton, "alpha", 0f, 1f)
                    val fadeInText = ObjectAnimator.ofFloat(buttonText, "alpha", 0f, 1f)
                    fadeInImage.setDuration(300)
                    fadeInText.setDuration(300)
                    fadeInImage.start()
                    fadeInText.start()
                }
            })
            fadeOut.start()
        }
    }

    private fun startTimerNotification() {
        val serviceIntent = Intent(this, TimerNotificationService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopTimerNotification() {
        val serviceIntent = Intent(this, TimerNotificationService::class.java)
        stopService(serviceIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "Timer Channel"
            val description = "Canal para el temporizador"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("timer_channel", name, importance)
            channel.description = description

            // Registrar el canal con el sistema
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private val isServiceRunning: Boolean
        get() {
            val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Int.MAX_VALUE)) {
                if (TimerNotificationService::class.java.name == service.service.className) {
                    return true
                }
            }
            return false
        }

    companion object {
        fun formatTime(millis: Long): String {
            val seconds = (millis / 1000) % 60 // Convertir a segundos y obtener el residuo de 60
            val minutes =
                (millis / (1000 * 60)) % 60 // Convertir a minutos y obtener el residuo de 60
            val hours = millis / (1000 * 60 * 60) // Convertir a horas

            return String.format(
                Locale.US,
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            ) // Formato HH:MM:SS
        }
    }
}