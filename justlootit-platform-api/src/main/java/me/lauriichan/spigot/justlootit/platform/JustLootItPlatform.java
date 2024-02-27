package me.lauriichan.spigot.justlootit.platform;

import org.bukkit.plugin.Plugin;

public abstract class JustLootItPlatform {
    
    protected final Plugin plugin;
    
    public JustLootItPlatform(Plugin plugin) {
        this.plugin = plugin;
    }
    
    public final Plugin plugin() {
        return plugin;
    }
    
    public abstract Scheduler scheduler();

}
