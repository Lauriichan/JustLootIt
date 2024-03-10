package me.lauriichan.spigot.justlootit.platform;

import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.scheduler.Scheduler;

public abstract class JustLootItPlatform {
    
    protected final Plugin plugin;
    protected final ISimpleLogger logger;
    
    public JustLootItPlatform(Plugin plugin, ISimpleLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }
    
    public final Plugin plugin() {
        return plugin;
    }
    
    public final ISimpleLogger logger() {
        return logger;
    }
    
    public abstract Scheduler scheduler();

}
