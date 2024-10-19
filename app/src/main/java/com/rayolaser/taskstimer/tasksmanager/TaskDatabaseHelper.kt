package com.rayolaser.taskstimer.tasksmanager

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val DATABASE_NAME = "tasks.db"
private const val DATABASE_VERSION = 5
private const val TABLE_NAME = "tasks"
private const val COLUMN_TASK_NAME = "task_name"
private const val COLUMN_TIME = "time"
private const val COLUMN_ID = "id_task"
private const val COLUMN_DATE = "task_date"

class TaskDatabaseHelper(context: Context?) :

    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TASK_NAME + " TEXT, " +
                COLUMN_TIME + " INTEGER, " +
                COLUMN_DATE + " TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 5) {
            // Crear una nueva tabla
            db.execSQL(
                "CREATE TABLE " + "tasks_new" + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +  // Nuevo campo id
                        COLUMN_TASK_NAME + " TEXT, " +
                        COLUMN_TIME + " INTEGER, " +
                        COLUMN_DATE + " TEXT)"
            )

            // Copiar los datos de la tabla antigua a la nueva
            db.execSQL(
                "INSERT INTO tasks_new (" + COLUMN_TASK_NAME + ", " + COLUMN_TIME + ") " +
                        "SELECT " + COLUMN_TASK_NAME + ", " + COLUMN_TIME + " FROM " + TABLE_NAME
            )

            // Eliminar la tabla antigua
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")

            // Renombrar la nueva tabla para que tenga el nombre original
            db.execSQL("ALTER TABLE tasks_new RENAME TO $TABLE_NAME")
        }
    }

    fun saveTask(taskName: String, time: Long) {
        val db = this.writableDatabase
        val values = ContentValues()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = dateFormat.format(calendar.time)
        //String date = calendar.get(Calendar.DAY_OF_MONTH) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR);
        Log.d("TaskDatabaseHelper", "saveTask: taskName: $taskName, time: $time, date: $date")
        values.put(COLUMN_TASK_NAME, taskName)
        values.put(COLUMN_TIME, time)
        values.put(COLUMN_DATE, date)

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getTasks(date: Date): Cursor {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formattedDate = dateFormat.format(date)
        val whereClause = " WHERE $COLUMN_DATE = '$formattedDate'"
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME$whereClause ORDER BY $COLUMN_ID DESC"
        //String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " DESC";
        Log.d("TaskDatabaseHelper", "getTasks: query: $query")
        val cursor = db.rawQuery(query, null)
        val count = cursor.count
        Log.d("TaskDatabaseHelper", "getTasks: cursor count: $count")
        return cursor
    }

    fun deleteTask(taskId: String) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(taskId))
        db.close()
    }
}

