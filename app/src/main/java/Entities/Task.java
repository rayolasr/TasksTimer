package Entities;

public class Task {
    private int idTask;
    private String taskName;
    private long time;

    public Task(int idTask, String taskName, long time) {
        this.idTask = idTask;
        this.taskName = taskName;
        this.time = time;
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