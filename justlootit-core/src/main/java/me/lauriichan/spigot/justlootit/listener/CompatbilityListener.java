package me.lauriichan.spigot.justlootit.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.listener.IListenerExtension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.util.CompatDependency;

@Extension
public final class CompatbilityListener implements IListenerExtension {

    private final JustLootItPlugin plugin;

    public CompatbilityListener(final JustLootItPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnable(PluginEnableEvent event) {
        CompatDependency.handleUpdate(plugin, event.getPlugin(), true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisable(PluginDisableEvent event) {
        CompatDependency.handleUpdate(plugin, event.getPlugin(), false);
    }

}
