package me.lauriichan.spigot.justlootit.platform.scheduler;

import me.lauriichan.laylib.logger.ISimpleLogger;

public final class SimpleTask<E> extends Task<E> {
    
    public static <T> SimpleTask<T> newCompleted(ISimpleLogger logger) {
        return newCompleted(logger, null);
    }
    
    public static <T> SimpleTask<T> newCompleted(ISimpleLogger logger, T value) {
        SimpleTask<T> task = new SimpleTask<>(logger);
        task.complete(value);
        return task;
    }

    private volatile boolean cancelled = false;

    public SimpleTask(ISimpleLogger logger) {
        super(logger, false);
    }

    @Override
    protected void doCancel() {
        this.cancelled = true;
    }

    @Override
    protected boolean isCancelled() {
        return cancelled;
    }

}
