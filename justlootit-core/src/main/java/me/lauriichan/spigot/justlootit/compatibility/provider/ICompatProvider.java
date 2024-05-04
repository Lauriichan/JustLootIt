package me.lauriichan.spigot.justlootit.compatibility.provider;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import me.lauriichan.minecraft.pluginbase.extension.ExtensionPoint;
import me.lauriichan.minecraft.pluginbase.extension.IExtension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;

@ExtensionPoint
public interface ICompatProvider extends IExtension {
    
    void onEnable(JustLootItPlugin jli, Plugin plugin);
    
    void onDisable(JustLootItPlugin jli, Plugin plugin);
    
    default PluginManager pluginManager() {
        return Bukkit.getServer().getPluginManager();
    }
    
}
