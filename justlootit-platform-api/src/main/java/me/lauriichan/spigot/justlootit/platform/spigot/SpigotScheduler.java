package me.lauriichan.spigot.justlootit.platform.spigot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import me.lauriichan.spigot.justlootit.platform.Scheduler;

public final class SpigotScheduler extends Scheduler {
    
    private final Plugin plugin;
    private final BukkitScheduler scheduler = Bukkit.getScheduler();
    
    public SpigotScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sync(Runnable runnable) {
        scheduler.runTask(plugin, runnable);
    }

    @Override
    public void syncLater(Runnable runnable, long delayTicks) {
        scheduler.runTaskLater(plugin, runnable, delayTicks);
    }

    @Override
    public void syncRepeat(Runnable runnable, long delayTicks, long repeatTicks) {
        scheduler.runTaskTimer(plugin, runnable, delayTicks, repeatTicks);
    }

    @Override
    public void async(Runnable runnable) {
        scheduler.runTaskAsynchronously(plugin, runnable);
    }

    @Override
    public void asyncLater(Runnable runnable, long delayTicks) {
        scheduler.runTaskLaterAsynchronously(plugin, runnable, delayTicks);
    }

    @Override
    public void asyncRepeat(Runnable runnable, long delayTicks, long repeatTicks) {
        scheduler.runTaskTimerAsynchronously(plugin, runnable, delayTicks, repeatTicks);
    }

}
