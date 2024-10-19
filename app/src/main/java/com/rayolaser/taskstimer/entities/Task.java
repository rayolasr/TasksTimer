package com.rayolaser.taskstimer.entities;

/** @noinspection unused*/
public class Task {
    private int idTask;
    private String taskName;
    private long time;
    private String date;

    public Task(int idTask, String taskName, long time, String date) {
        this.idTask = idTask;
        this.taskName = taskName;
        this.time = time;
        this.date = date;
    }

    public void setIdTask(int idTask) {
        this.idTask = idTask;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public int getIdTask() {
        return idTask;
    }

    public String getTaskName() {
        return taskName;
    }

    public long getTime() {
        return time;
    }
}