package me.lauriichan.spigot.justlootit.compatibility.provider.iris;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.compatibility.provider.Compatibility;
import me.lauriichan.spigot.justlootit.compatibility.provider.ICompatProvider;

@Extension
@Compatibility(name = "Iris", minMajor = 2, minMinor = 8)
public class IrisCompatProvider implements ICompatProvider {

    private Listener listener;

    @Override
    public void onEnable(JustLootItPlugin jli, Plugin plugin) {
        pluginManager().registerEvents(listener = new IrisListener(plugin.getName(), jli.versionHandler(), jli.configManager()), plugin);
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
