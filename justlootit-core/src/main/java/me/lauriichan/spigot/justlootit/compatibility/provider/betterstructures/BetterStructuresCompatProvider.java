package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.compatibility.provider.Compatibility;

@Extension
@Compatibility(name = "BetterStructures", minMajor = 1, minMinor = 0, maxMajor = 1)
public class BetterStructuresCompatProvider implements IBetterStructuresProvider {
    
    private Listener listener;
    private IBetterStructuresAccess access;
    
    @Override
    public void onEnable(JustLootItPlugin jli, Plugin plugin) {
        pluginManager().registerEvents(listener = createSchematicListener(jli, plugin), jli);
        access = new BetterStructuresAccess(plugin.getName());
    }

    @Override
    public void onDisable(JustLootItPlugin jli, Plugin plugin) {
        HandlerList.unregisterAll(listener);
        clear();
    }
    
    protected Listener createSchematicListener(JustLootItPlugin jli, Plugin plugin) {
        return new BetterStructuresListener(plugin.getName(), jli.versionHandler(), jli.configManager());
    }
    
    protected void clear() {
        listener = null;
        access = null;
    }

    @Override
    public IBetterStructuresAccess access() {
        return access;
    }

}
