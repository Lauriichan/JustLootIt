package me.lauriichan.spigot.justlootit.platform.scheduler;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.logger.ISimpleLogger;

public abstract class Scheduler {
    
    private static class CompletedTask<E> extends Task<E> {

        public CompletedTask(ISimpleLogger logger, boolean repeating) {
            super(logger, repeating);
        }

        @Override
        protected void doCancel() {}

        @Override
        protected boolean isCancelled() {
            return false;
        }
        
    }

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
    
    public boolean isRegional() {
        return false;
    }

    public void shutdown() {}

    public Task<Void> entity(final Entity entity, final Runnable runnable) {
        return runDirect(runnable);
    }

    public <E> Task<E> entity(final Entity entity, final Supplier<E> supplier) {
        return runDirect(supplier);
    }

    public Task<Void> syncEntity(final Entity entity, final Runnable runnable) {
        return sync(runnable);
    }

    public Task<Void> syncEntityLater(final Entity entity, final Runnable runnable, final long delayTicks) {
        return syncLater(runnable, delayTicks);
    }

    public <E> Task<E> syncEntity(final Entity entity, final Supplier<E> supplier) {
        return sync(supplier);
    }

    public <E> Task<E> syncEntityLater(final Entity entity, final Supplier<E> supplier, final long delayTicks) {
        return syncLater(supplier, delayTicks);
    }

    public Task<Void> syncEntityRepeat(final Entity entity, final Runnable runnable, final long delayTicks, final long repeatTicks) {
        return syncRepeat(runnable, delayTicks, repeatTicks);
    }

    public Task<Void> asyncEntity(final Entity entity, final Runnable runnable) {
        return async(runnable);
    }

    public Task<Void> asyncEntityLater(final Entity entity, final Runnable runnable, final long delayTicks) {
        return asyncLater(runnable, delayTicks);
    }

    public <E> Task<E> asyncEntity(final Entity entity, final Supplier<E> supplier) {
        return async(supplier);
    }

    public <E> Task<E> asyncEntityLater(final Entity entity, final Supplier<E> supplier, final long delayTicks) {
        return asyncLater(supplier, delayTicks);
    }

    public Task<Void> asyncEntityRepeat(final Entity entity, final Runnable runnable, final long delayTicks, final long repeatTicks) {
        return asyncRepeat(runnable, delayTicks, repeatTicks);
    }

    public Task<Void> regional(final Location location, final Runnable runnable) {
        return runDirect(runnable);
    }

    public <E> Task<E> regional(final Location location, final Supplier<E> supplier) {
        return runDirect(supplier);
    }

    public Task<Void> syncRegional(final Location location, final Runnable runnable) {
        return sync(runnable);
    }

    public Task<Void> syncRegionalLater(final Location location, final Runnable runnable, final long delayTicks) {
        return syncLater(runnable, delayTicks);
    }

    public <E> Task<E> syncRegional(final Location location, final Supplier<E> supplier) {
        return sync(supplier);
    }

    public <E> Task<E> syncRegionalLater(final Location location, final Supplier<E> supplier, final long delayTicks) {
        return syncLater(supplier, delayTicks);
    }

    public Task<Void> syncRegionalRepeat(final Location location, final Runnable runnable, final long delayTicks, final long repeatTicks) {
        return asyncRepeat(runnable, delayTicks, repeatTicks);
    }

    public Task<Void> asyncRegional(final Location location, final Runnable runnable) {
        return async(runnable);
    }

    public Task<Void> asyncRegionalLater(final Location location, final Runnable runnable, final long delayTicks) {
        return asyncLater(runnable, delayTicks);
    }

    public <E> Task<E> asyncRegional(final Location location, final Supplier<E> supplier) {
        return async(supplier);
    }

    public <E> Task<E> asyncRegionalLater(final Location location, final Supplier<E> supplier, final long delayTicks) {
        return asyncLater(supplier, delayTicks);
    }

    public Task<Void> asyncRegionalRepeat(final Location location, final Runnable runnable, final long delayTicks, final long repeatTicks) {
        return asyncRepeat(runnable, delayTicks, repeatTicks);
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
    
    /*
     * Helper
     */
    
    protected final Task<Void> runDirect(Runnable runnable) {
        CompletedTask<Void> task = new CompletedTask<>(logger, false);
        try {
            runnable.run();
        } catch(RuntimeException exp) {
            task.logger().error("Failed to complete scheduled task", exp);
        }
        task.complete(null);
        return task;
    }
    
    private <E> Task<E> runDirect(Supplier<E> supplier) {
        CompletedTask<E> task = new CompletedTask<>(logger, false);
        try {
            task.complete(supplier.get());
        } catch(RuntimeException exp) {
            task.logger().error("Failed to complete scheduled task", exp);
            task.complete(null);
        }
        return task;
    }

}
