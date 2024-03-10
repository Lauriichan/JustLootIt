package me.lauriichan.spigot.justlootit.platform.scheduler;

public enum TaskState {
    
    IDLE,
    RUNNING,
    CANCELLED(true),
    DONE(true);
    
    private final boolean completed;
    
    private TaskState() {
        this(false);
    }
    
    private TaskState(boolean completed) {
        this.completed = completed;
    }
    
    public boolean completed() {
        return completed;
    }
    
}
