package me.lauriichan.spigot.justlootit.compatibility.provider.worldguard;

import org.bukkit.plugin.Plugin;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.compatibility.provider.Compatibility;
import me.lauriichan.spigot.justlootit.compatibility.provider.ICompatProvider;
import me.lauriichan.spigot.justlootit.compatibility.support.AccessSupport;

@Extension
@Compatibility(name = "WorldGuard", minMajor = 7, minMinor = 0)
public class WorldGuardCompatProvider implements ICompatProvider {

    private IWorldGuardAccess access;

    @Override
    public void onLoad(JustLootItPlugin jli, Plugin plugin) {
        this.access = new WorldGuardAccess(jli.logger(), plugin);
    }

    @Override
    public void onEnable(JustLootItPlugin jli, Plugin plugin) {
        access.init();
        AccessSupport.INSTANCE.register(access);
    }

    @Override
    public void onDisable(JustLootItPlugin jli, Plugin plugin) {
        AccessSupport.INSTANCE.unregister(access);
    }

}
