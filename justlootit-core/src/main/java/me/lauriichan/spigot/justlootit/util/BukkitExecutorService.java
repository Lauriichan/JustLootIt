package me.lauriichan.spigot.justlootit.util;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class BukkitExecutorService extends AbstractExecutorService {

    private final Plugin plugin;
    private final BukkitScheduler scheduler = Bukkit.getScheduler();

    private final boolean async;

    public BukkitExecutorService(Plugin plugin, boolean async) {
        this.plugin = plugin;
        this.async = async;
    }

    public boolean isAsync() {
        return async;
    }

    @Override
    public void shutdown() {}

    @Override
    public List<Runnable> shutdownNow() {
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        if (!plugin.isEnabled()) {
            return;
        }
        if (async) {
            scheduler.runTaskAsynchronously(plugin, command);
            return;
        }
        scheduler.runTask(plugin, command);
    }

}