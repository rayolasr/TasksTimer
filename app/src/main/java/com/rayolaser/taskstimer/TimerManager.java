package com.rayolaser.taskstimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TimerManager {

    private static final String PREFS_NAME = "TimerPrefs";
    private static final String KEY_START_TIME = "startTime";
    private static final String KEY_ACCUMULATED_TIME = "accumulatedTime";
    private static final String KEY_IS_RUNNING = "isRunning";
    private static final String KEY_IS_PAUSED = "isPaused";

    private final SharedPreferences sharedPreferences;
    private boolean isRunning;
    private long startTime;
    private long accumulatedTime;
    private boolean isPaused;

    public TimerManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadState();
    }

    // Método para empezar el temporizador
    public void start() {
        Log.d("TimerManager", "Starting timer...");
        Log.d("TimerManager", "isRunning: " + isRunning);
        if (!isRunning || isPaused) {
            startTime = System.currentTimeMillis();
            isRunning = true;
            isPaused = false;
            saveState();
        }
    }

    // Método para pausar el temporizador
    public void pause() {
        if (isRunning || !isPaused) {
            accumulatedTime += System.currentTimeMillis() - startTime;
            isPaused = true;
            saveState();
        }
    }

    // Obtener el tiempo transcurrido en milisegundos
    public long getElapsedTime() {
        if (isRunning && !isPaused) {
            return accumulatedTime + (System.currentTimeMillis() - startTime);
        } else {
            return accumulatedTime;
        }
    }

    // Reiniciar el temporizador
    public void reset() {
        startTime = 0;
        accumulatedTime = 0;
        isRunning = false;
        saveState();
    }

    // Saber si el temporizador está corriendo
    public boolean isRunning() {
        return isRunning;
    }

    public boolean isPaused() { return isPaused; }

    // Guardar el estado del temporizador en SharedPreferences
    private void saveState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_RUNNING, isRunning);
        editor.putLong(KEY_START_TIME, startTime);
        editor.putLong(KEY_ACCUMULATED_TIME, accumulatedTime);
        editor.putBoolean(KEY_IS_PAUSED, isPaused);
        editor.apply();
    }

    // Cargar el estado del temporizador desde SharedPreferences
    public void loadState() {
        isRunning = sharedPreferences.getBoolean(KEY_IS_RUNNING, false);
        startTime = sharedPreferences.getLong(KEY_START_TIME, 0);
        accumulatedTime = sharedPreferences.getLong(KEY_ACCUMULATED_TIME, 0);
        isPaused = sharedPreferences.getBoolean(KEY_IS_PAUSED, false);
    }
}