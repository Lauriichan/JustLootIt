package me.lauriichan.spigot.justlootit.platform.spigot;

import org.bukkit.scheduler.BukkitTask;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.scheduler.Task;

public final class SpigotTask<E> extends Task<E> {

    private volatile BukkitTask task;

    public SpigotTask(ISimpleLogger logger, boolean repeating) {
        super(logger, repeating);
    }
    
    final void task(BukkitTask task) {
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
