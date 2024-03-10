package me.lauriichan.spigot.justlootit.platform.scheduler;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.logger.ISimpleLogger;

public abstract class Scheduler {

    protected final Plugin plugin;
    protected final ISimpleLogger logger;

    private final Executor syncExecutor = runnable -> sync(runnable);
    private final Executor asyncExecutor = runnable -> async(runnable);

    public Scheduler(Plugin plugin, ISimpleLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    public final Plugin plugin() {
        return plugin;
    }

    public final ISimpleLogger logger() {
        return logger;
    }

    public final Executor syncExecutor() {
        return syncExecutor;
    }

    public final Executor asyncExecutor() {
        return asyncExecutor;
    }

    public void shutdown() {}

    public Task<Void> entity(final Entity entity, final Runnable runnable) {
        return sync(runnable);
    }

    public Task<Void> entityLater(final Entity entity, final Runnable runnable, final long delayTicks) {
        return syncLater(runnable, delayTicks);
    }

    public <E> Task<E> entity(final Entity entity, final Supplier<E> supplier) {
        return sync(supplier);
    }

    public <E> Task<E> entityLater(final Entity entity, final Supplier<E> supplier, final long delayTicks) {
        return syncLater(supplier, delayTicks);
    }

    public Task<Void> entityRepeat(final Entity entity, final Runnable runnable, final long delayTicks, final long repeatTicks) {
        return syncRepeat(runnable, delayTicks, repeatTicks);
    }

    public Task<Void> regional(final Location location, final Runnable runnable) {
        return sync(runnable);
    }

    public Task<Void> regionalLater(final Location location, final Runnable runnable, final long delayTicks) {
        return syncLater(runnable, delayTicks);
    }

    public <E> Task<E> regional(final Location location, final Supplier<E> supplier) {
        return sync(supplier);
    }

    public <E> Task<E> regionalLater(final Location location, final Supplier<E> supplier, final long delayTicks) {
        return syncLater(supplier, delayTicks);
    }

    public Task<Void> regionalRepeat(final Location location, final Runnable runnable, final long delayTicks, final long repeatTicks) {
        return syncRepeat(runnable, delayTicks, repeatTicks);
    }

    public abstract Task<Void> sync(final Runnable runnable);

    public abstract Task<Void> syncLater(final Runnable runnable, final long delayTicks);

    public abstract <E> Task<E> sync(final Supplier<E> supplier);

    public abstract <E> Task<E> syncLater(final Supplier<E> supplier, final long delayTicks);

    public abstract Task<Void> syncRepeat(final Runnable runnable, final long delayTicks, final long repeatTicks);

    public abstract Task<Void> async(final Runnable runnable);

    public abstract Task<Void> asyncLater(final Runnable runnable, final long delayTicks);

    public abstract <E> Task<E> async(final Supplier<E> supplier);

    public abstract <E> Task<E> asyncLater(final Supplier<E> supplier, final long delayTicks);

    public abstract Task<Void> asyncRepeat(final Runnable runnable, final long delayTicks, final long repeatTicks);

}
