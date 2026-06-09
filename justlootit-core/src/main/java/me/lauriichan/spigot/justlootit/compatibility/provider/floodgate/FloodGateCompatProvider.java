package me.lauriichan.spigot.justlootit.compatibility.provider.floodgate;

import org.bukkit.plugin.Plugin;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.compatibility.provider.Compatibility;

@Extension
@Compatibility(name = "floodgate", minMajor = 2, minMinor = 0)
public class FloodGateCompatProvider implements IFloodGateProvider {

    private IFloodGateAccess access;

    @Override
    public void onEnable(JustLootItPlugin jli, Plugin plugin) {
        access = new FloodGateAccess();
    }

    @Override
    public void onDisable(JustLootItPlugin jli, Plugin plugin) {
        access = null;
    }

    @Override
    public IFloodGateAccess access() {
        return access;
    }

}
