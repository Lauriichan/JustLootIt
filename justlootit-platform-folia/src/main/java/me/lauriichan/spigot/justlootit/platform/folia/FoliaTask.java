package me.lauriichan.spigot.justlootit.platform.folia;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.scheduler.Task;

public final class FoliaTask<E> extends Task<E> {

    private volatile ScheduledTask task;

    public FoliaTask(ISimpleLogger logger, boolean repeating) {
        super(logger, repeating);
    }
    
    final void task(ScheduledTask task) {
        if (this.task != null) {
            return;
        }
        this.task = task;
    }

    @Override
    protected void doCancel() {
        task.cancel();
    }

    @Override
    protected boolean isCancelled() {
        return task.isCancelled();
    }

}
