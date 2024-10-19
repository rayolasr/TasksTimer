package com.rayolaser.taskstimer

import android.content.Context
import android.content.SharedPreferences
import android.util.Log


private const val PREFS_NAME = "TimerPrefs"
private const val KEY_START_TIME = "startTime"
private const val KEY_ACCUMULATED_TIME = "accumulatedTime"
private const val KEY_IS_RUNNING = "isRunning"
private const val KEY_IS_PAUSED = "isPaused"

class TimerManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Saber si el temporizador está corriendo
    var isRunning: Boolean = false
    var isPaused: Boolean = false
    private var startTime: Long = 0
    private var accumulatedTime: Long = 0

    init {
        loadState()
    }

    // Método para empezar el temporizador
    fun start() {
        Log.d("TimerManager", "Starting timer...")
        Log.d("TimerManager", "isRunning: $isRunning")
        if (!isRunning || isPaused) {
            startTime = System.currentTimeMillis()
            isRunning = true
            isPaused = false
            saveState()
        }
    }

    val elapsedTime: Long
        // Obtener el tiempo transcurrido en milisegundos
        get() = if (isRunning && !isPaused) {
            accumulatedTime + (System.currentTimeMillis() - startTime)
        } else {
            accumulatedTime
        }

    // Reiniciar el temporizador
    fun reset() {
        startTime = 0
        accumulatedTime = 0
        isRunning = false
        saveState()
    }

    // Guardar el estado del temporizador en SharedPreferences
    private fun saveState() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_RUNNING, isRunning)
        editor.putLong(KEY_START_TIME, startTime)
        editor.putLong(KEY_ACCUMULATED_TIME, accumulatedTime)
        editor.putBoolean(KEY_IS_PAUSED, isPaused)
        editor.apply()
    }

    // Cargar el estado del temporizador desde SharedPreferences
    private fun loadState() {
        isRunning = sharedPreferences.getBoolean(KEY_IS_RUNNING, false)
        startTime = sharedPreferences.getLong(KEY_START_TIME, 0)
        accumulatedTime = sharedPreferences.getLong(KEY_ACCUMULATED_TIME, 0)
        isPaused = sharedPreferences.getBoolean(KEY_IS_PAUSED, false)
    }
}