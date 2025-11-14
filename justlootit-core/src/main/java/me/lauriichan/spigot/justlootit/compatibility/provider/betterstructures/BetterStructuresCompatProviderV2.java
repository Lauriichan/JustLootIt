package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.compatibility.provider.Compatibility;

@Extension
@Compatibility(name = "BetterStructures", minMajor = 2, minMinor = 0)
public class BetterStructuresCompatProviderV2 extends BetterStructuresCompatProvider {

    private Listener listener;

    @Override
    public void onEnable(JustLootItPlugin jli, Plugin plugin) {
        pluginManager().registerEvents(
            listener = new BetterStructuresModularListener(plugin.getName(), jli.versionHandler(), jli.configManager()), jli);

        super.onEnable(jli, plugin);
    }

    @Override
    public void onDisable(JustLootItPlugin jli, Plugin plugin) {
        HandlerList.unregisterAll(listener);

        super.onDisable(jli, plugin);
    }

    @Override
    protected void clear() {
        listener = null;

        super.clear();
    }

}
