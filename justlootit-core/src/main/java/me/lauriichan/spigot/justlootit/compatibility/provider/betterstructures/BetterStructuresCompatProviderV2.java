package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.compatibility.provider.Compatibility;

@Extension
@Compatibility(name = "BetterStructures", minMajor = 2, minMinor = 1)
public class BetterStructuresCompatProviderV2 extends BetterStructuresCompatProvider {
    
    @Override
    protected Listener createSchematicListener(JustLootItPlugin jli, Plugin plugin) {
        return new BetterStructuresListenerV2(plugin.getName(), jli.versionHandler(), jli.configManager());
    }

}
