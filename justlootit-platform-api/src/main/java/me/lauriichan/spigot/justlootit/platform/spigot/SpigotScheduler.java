package me.lauriichan.spigot.justlootit.platform.spigot;

import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.scheduler.Scheduler;
import me.lauriichan.spigot.justlootit.platform.scheduler.Task;

public final class SpigotScheduler extends Scheduler {

    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    public SpigotScheduler(Plugin plugin, ISimpleLogger logger) {
        super(plugin, logger);
    }

    @Override
    public Task<Void> sync(Runnable runnable) {
        SpigotTask<Void> task = new SpigotTask<>(logger, false);
        task.task(scheduler.runTask(plugin, catchRunnable(task, runnable)));
        return task;
    }

    @Override
    public Task<Void> syncLater(Runnable runnable, long delayTicks) {
        SpigotTask<Void> task = new SpigotTask<>(logger, false);
        task.task(scheduler.runTaskLater(plugin, catchRunnable(task, runnable), delayTicks));
        return task;
    }

    @Override
    public <E> Task<E> sync(Supplier<E> supplier) {
        SpigotTask<E> task = new SpigotTask<>(logger, false);
        task.task(scheduler.runTask(plugin, toRunnable(task, supplier)));
        return task;
    }

    @Override
    public <E> Task<E> syncLater(Supplier<E> supplier, long delayTicks) {
        SpigotTask<E> task = new SpigotTask<>(logger, false);
        task.task(scheduler.runTaskLater(plugin, toRunnable(task, supplier), delayTicks));
        return task;
    }

    @Override
    public Task<Void> syncRepeat(Runnable runnable, long delayTicks, long repeatTicks) {
        SpigotTask<Void> task = new SpigotTask<>(logger, true);
        task.task(scheduler.runTaskTimer(plugin, catchRunnable(task, runnable), delayTicks, repeatTicks));
        return task;
    }

    @Override
    public Task<Void> async(Runnable runnable) {
        SpigotTask<Void> task = new SpigotTask<>(logger, false);
        task.task(scheduler.runTaskAsynchronously(plugin, catchRunnable(task, runnable)));
        return task;
    }

    @Override
    public Task<Void> asyncLater(Runnable runnable, long delayTicks) {
        SpigotTask<Void> task = new SpigotTask<>(logger, false);
        task.task(scheduler.runTaskLaterAsynchronously(plugin, catchRunnable(task, runnable), delayTicks));
        return task;
    }

    @Override
    public <E> Task<E> async(Supplier<E> supplier) {
        SpigotTask<E> task = new SpigotTask<>(logger, false);
        task.task(scheduler.runTaskAsynchronously(plugin, toRunnable(task, supplier)));
        return task;
    }

    @Override
    public <E> Task<E> asyncLater(Supplier<E> supplier, long delayTicks) {
        SpigotTask<E> task = new SpigotTask<>(logger, false);
        task.task(scheduler.runTaskLater(plugin, toRunnable(task, supplier), delayTicks));
        return task;
    }

    @Override
    public Task<Void> asyncRepeat(Runnable runnable, long delayTicks, long repeatTicks) {
        SpigotTask<Void> task = new SpigotTask<>(logger, true);
        task.task(scheduler.runTaskTimerAsynchronously(plugin, catchRunnable(task, runnable), delayTicks, repeatTicks));
        return task;
    }

    /*
     * Helper
     */

    private final Runnable catchRunnable(SpigotTask<Void> task, Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (RuntimeException re) {
                task.logger().error("Failed to complete scheduled task", re);
            }
            task.complete(null);
        };
    }

    private final <E> Runnable toRunnable(SpigotTask<E> task, Supplier<E> supplier) {
        return () -> {
            try {
                task.complete(supplier.get());
            } catch (RuntimeException re) {
                task.logger().error("Failed to complete scheduled task", re);
                task.complete(null);
            }
        };
    }

}
