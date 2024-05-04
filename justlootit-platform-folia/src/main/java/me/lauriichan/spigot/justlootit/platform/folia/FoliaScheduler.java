package me.lauriichan.spigot.justlootit.platform.folia;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.scheduler.Scheduler;
import me.lauriichan.spigot.justlootit.platform.scheduler.Task;

public final class FoliaScheduler extends Scheduler {

    private final GlobalRegionScheduler global;
    private final RegionScheduler region;
    private final AsyncScheduler async;

    public FoliaScheduler(Plugin plugin, ISimpleLogger logger) {
        super(plugin, logger);
        final Server server = plugin.getServer();
        this.global = server.getGlobalRegionScheduler();
        this.region = server.getRegionScheduler();
        this.async = server.getAsyncScheduler();
    }
    
    @Override
    public boolean isRegional() {
        return true;
    }
    
    @Override
    public Task<Void> entity(Entity entity, Runnable runnable) {
        return syncEntity(entity, runnable);
    }
    
    @Override
    public <E> Task<E> entity(Entity entity, Supplier<E> supplier) {
        return syncEntity(entity, supplier);
    }

    @Override
    public Task<Void> syncEntity(Entity entity, Runnable runnable) {
        FoliaTask<Void> task = new FoliaTask<>(logger, false);
        task.task(entity.getScheduler().run(plugin, wrap(task, runnable), task::cancel));
        return task;
    }

    @Override
    public Task<Void> syncEntityLater(Entity entity, Runnable runnable, long delayTicks) {
        FoliaTask<Void> task = new FoliaTask<>(logger, false);
        task.task(entity.getScheduler().runDelayed(plugin, wrap(task, runnable), task::cancel, delayTicks));
        return task;
    }

    @Override
    public <E> Task<E> syncEntity(Entity entity, Supplier<E> supplier) {
        FoliaTask<E> task = new FoliaTask<>(logger, false);
        task.task(entity.getScheduler().run(plugin, wrap(task, supplier), task::cancel));
        return task;
    }

    @Override
    public <E> Task<E> syncEntityLater(Entity entity, Supplier<E> supplier, long delayTicks) {
        FoliaTask<E> task = new FoliaTask<>(logger, false);
        task.task(entity.getScheduler().runDelayed(plugin, wrap(task, supplier), task::cancel, delayTicks));
        return task;
    }

    @Override
    public Task<Void> syncEntityRepeat(Entity entity, Runnable runnable, long delayTicks, long repeatTicks) {
        FoliaTask<Void> task = new FoliaTask<>(logger, true);
        task.task(entity.getScheduler().runAtFixedRate(plugin, wrap(task, runnable), task::cancel, delayTicks, repeatTicks));
        return task;
    }

    @Override
    public Task<Void> asyncEntity(Entity entity, Runnable runnable) {
        return syncEntity(entity, runnable);
    }

    @Override
    public Task<Void> asyncEntityLater(Entity entity, Runnable runnable, long delayTicks) {
        return syncEntityLater(entity, runnable, delayTicks);
    }

    @Override
    public <E> Task<E> asyncEntity(Entity entity, Supplier<E> supplier) {
        return syncEntity(entity, supplier);
    }

    @Override
    public <E> Task<E> asyncEntityLater(Entity entity, Supplier<E> supplier, long delayTicks) {
        return syncEntityLater(entity, supplier, delayTicks);
    }

    @Override
    public Task<Void> asyncEntityRepeat(Entity entity, Runnable runnable, long delayTicks, long repeatTicks) {
        return syncEntityRepeat(entity, runnable, delayTicks, repeatTicks);
    }
    
    @Override
    public Task<Void> regional(Location location, Runnable runnable) {
        return syncRegional(location, runnable);
    }
    
    @Override
    public <E> Task<E> regional(Location location, Supplier<E> supplier) {
        return syncRegional(location, supplier);
    }

    @Override
    public Task<Void> syncRegional(Location location, Runnable runnable) {
        FoliaTask<Void> task = new FoliaTask<>(logger, false);
        task.task(region.run(plugin, location, wrap(task, runnable)));
        return task;
    }

    @Override
    public Task<Void> syncRegionalLater(Location location, Runnable runnable, long delayTicks) {
        FoliaTask<Void> task = new FoliaTask<>(logger, false);
        task.task(region.runDelayed(plugin, location, wrap(task, runnable), delayTicks));
        return task;
    }

    @Override
    public <E> Task<E> syncRegional(Location location, Supplier<E> supplier) {
        FoliaTask<E> task = new FoliaTask<>(logger, false);
        task.task(region.run(plugin, location, wrap(task, supplier)));
        return task;
    }

    @Override
    public <E> Task<E> syncRegionalLater(Location location, Supplier<E> supplier, long delayTicks) {
        FoliaTask<E> task = new FoliaTask<>(logger, false);
        task.task(region.runDelayed(plugin, location, wrap(task, supplier), delayTicks));
        return task;
    }

    @Override
    public Task<Void> syncRegionalRepeat(Location location, Runnable runnable, long delayTicks, long repeatTicks) {
        FoliaTask<Void> task = new FoliaTask<>(logger, true);
        task.task(region.runAtFixedRate(plugin, location, wrap(task, runnable), delayTicks, repeatTicks));
        return task;
    }

    @Override
    public Task<Void> asyncRegional(Location location, Runnable runnable) {
        return syncRegional(location, runnable);
    }

    @Override
    public Task<Void> asyncRegionalLater(Location location, Runnable runnable, long delayTicks) {
        return syncRegionalLater(location, runnable, delayTicks);
    }

    @Override
    public <E> Task<E> asyncRegional(Location location, Supplier<E> supplier) {
        return syncRegional(location, supplier);
    }

    @Override
    public <E> Task<E> asyncRegionalLater(Location location, Supplier<E> supplier, long delayTicks) {
        return syncRegionalLater(location, supplier, delayTicks);
    }

    @Override
    public Task<Void> asyncRegionalRepeat(Location location, Runnable runnable, long delayTicks, long repeatTicks) {
        return syncRegionalRepeat(location, runnable, delayTicks, repeatTicks);
    }

    @Override
    public Task<Void> sync(Runnable runnable) {
        FoliaTask<Void> task = new FoliaTask<>(logger, false);
        task.task(global.run(plugin, wrap(task, runnable)));
        return task;
    }

    @Override
    public Task<Void> syncLater(Runnable runnable, long delayTicks) {
        FoliaTask<Void> task = new FoliaTask<>(logger, false);
        task.task(global.runDelayed(plugin, wrap(task, runnable), delayTicks));
        return task;
    }

    @Override
    public <E> Task<E> sync(Supplier<E> supplier) {
        FoliaTask<E> task = new FoliaTask<>(logger, false);
        task.task(global.run(plugin, wrap(task, supplier)));
        return task;
    }

    @Override
    public <E> Task<E> syncLater(Supplier<E> supplier, long delayTicks) {
        FoliaTask<E> task = new FoliaTask<>(logger, false);
        task.task(global.runDelayed(plugin, wrap(task, supplier), delayTicks));
        return task;
    }

    @Override
    public Task<Void> syncRepeat(Runnable runnable, long delayTicks, long repeatTicks) {
        FoliaTask<Void> task = new FoliaTask<>(logger, true);
        task.task(global.runAtFixedRate(plugin, wrap(task, runnable), delayTicks, repeatTicks));
        return task;
    }

    @Override
    public Task<Void> async(Runnable runnable) {
        FoliaTask<Void> task = new FoliaTask<>(logger, false);
        task.task(async.runNow(plugin, wrap(task, runnable)));
        return task;
    }

    @Override
    public Task<Void> asyncLater(Runnable runnable, long delayTicks) {
        FoliaTask<Void> task = new FoliaTask<>(logger, false);
        task.task(async.runDelayed(plugin, wrap(task, runnable), delayTicks * 50L, TimeUnit.MILLISECONDS));
        return task;
    }

    @Override
    public <E> Task<E> async(Supplier<E> supplier) {
        FoliaTask<E> task = new FoliaTask<>(logger, false);
        task.task(async.runNow(plugin, wrap(task, supplier)));
        return task;
    }

    @Override
    public <E> Task<E> asyncLater(Supplier<E> supplier, long delayTicks) {
        FoliaTask<E> task = new FoliaTask<>(logger, false);
        task.task(async.runDelayed(plugin, wrap(task, supplier), delayTicks * 50L, TimeUnit.MILLISECONDS));
        return task;
    }

    @Override
    public Task<Void> asyncRepeat(Runnable runnable, long delayTicks, long repeatTicks) {
        FoliaTask<Void> task = new FoliaTask<>(logger, true);
        task.task(async.runAtFixedRate(plugin, wrap(task, runnable), delayTicks * 50L, repeatTicks * 50L, TimeUnit.MILLISECONDS));
        return task;
    }

    /*
     * Helper
     */

    private final Consumer<ScheduledTask> wrap(FoliaTask<Void> task, Runnable runnable) {
        return (t) -> {
            try {
                runnable.run();
            } catch (RuntimeException re) {
                task.logger().error("Failed to complete scheduled task", re);
            }
            task.complete(null);
        };
    }

    private final <E> Consumer<ScheduledTask> wrap(FoliaTask<E> task, Supplier<E> supplier) {
        return (t) -> {
            try {
                task.complete(supplier.get());
            } catch (RuntimeException re) {
                task.logger().error("Failed to complete scheduled task", re);
                task.complete(null);
            }
        };
    }

}
