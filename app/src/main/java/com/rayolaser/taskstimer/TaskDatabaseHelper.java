package com.rayolaser.taskstimer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "tasks";
    private static final String COLUMN_TASK_NAME = "task_name";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_ID = "id_task";

    public TaskDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // Nuevo campo id
                COLUMN_TASK_NAME + " TEXT, " +
                COLUMN_TIME + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Crear una nueva tabla con el campo id_task
            db.execSQL("CREATE TABLE tasks_new (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TASK_NAME + " TEXT, " +
                    COLUMN_TIME + " INTEGER)");

            // Copiar los datos de la tabla antigua a la nueva
            db.execSQL("INSERT INTO tasks_new (" + COLUMN_TASK_NAME + ", " + COLUMN_TIME + ") " +
                    "SELECT " + COLUMN_TASK_NAME + ", " + COLUMN_TIME + " FROM " + TABLE_NAME);

            // Eliminar la tabla antigua
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

            // Renombrar la nueva tabla para que tenga el nombre original
            db.execSQL("ALTER TABLE tasks_new RENAME TO " + TABLE_NAME);
        }
    }

    public void saveTask(String taskName, long time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TASK_NAME, taskName);
        values.put(COLUMN_TIME, time);

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public Cursor getTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC", null); // Ordenar por antigüedad
    }

    public void deleteTask(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{taskId});
        db.close();
    }
}

