package me.lauriichan.spigot.justlootit.compatibility.provider.customstructures;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.compatibility.provider.Compatibility;

@Extension
@Compatibility(name = "CustomStructures", minMajor = 1, minMinor = 0)
public class CustomStructuresCompatProvider implements ICustomStructuresProvider {
    
    private Listener listener;
    private ICustomStructuresAccess access;
    
    @Override
    public void onEnable(JustLootItPlugin jli, Plugin plugin) {
        pluginManager().registerEvents(listener = new CustomStructuresListener(plugin.getName(), jli.versionHandler(), jli.configManager()), jli);
        access = new CustomStructuresAccess(plugin);
    }

    @Override
    public void onDisable(JustLootItPlugin jli, Plugin plugin) {
        HandlerList.unregisterAll(listener);
        clear();
    }
    
    private void clear() {
        listener = null;
        access = null;
    }

    @Override
    public ICustomStructuresAccess access() {
        return access;
    }

}
