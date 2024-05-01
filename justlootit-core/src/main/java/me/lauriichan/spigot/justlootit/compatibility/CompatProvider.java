package me.lauriichan.spigot.justlootit.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;

public abstract class CompatProvider {
    
    public abstract void onEnable(JustLootItPlugin jli, Plugin plugin);
    
    public abstract void onDisable(JustLootItPlugin jli, Plugin plugin);
    
    public final PluginManager pluginManager() {
        return Bukkit.getServer().getPluginManager();
    }
    
}
