package me.lauriichan.spigot.justlootit.compatibility.customstructures;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.compatibility.CompatProvider;

public class CustomStructuresCompatProvider extends CompatProvider {
    
    private Listener listener;
    
    @Override
    public void onEnable(JustLootItPlugin jli, Plugin plugin) {
        pluginManager().registerEvents(listener = new CustomStructuresListener(jli.versionHandler()), jli);
    }

    @Override
    public void onDisable(JustLootItPlugin jli, Plugin plugin) {
        HandlerList.unregisterAll(listener);
        clear();
    }
    
    private void clear() {
        listener = null;
    }

}
