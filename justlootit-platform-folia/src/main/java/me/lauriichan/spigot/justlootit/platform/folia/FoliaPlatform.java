package me.lauriichan.spigot.justlootit.platform.folia;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;
import me.lauriichan.spigot.justlootit.platform.PlatformType;
import me.lauriichan.spigot.justlootit.platform.version.ServerVersion;
import me.lauriichan.spigot.justlootit.platform.version.SimpleVersion;

public final class FoliaPlatform extends JustLootItPlatform {

    private final FoliaScheduler scheduler;

    public FoliaPlatform(Plugin plugin, ISimpleLogger logger) {
        super(plugin, logger);
        this.scheduler = new FoliaScheduler(plugin, logger);
    }

    @Override
    protected ServerVersion detectVersion() {
        return new ServerVersion(Bukkit.getServer().getClass().getPackage().getName() + ".%s",
            SimpleVersion.of(Bukkit.getServer().getMinecraftVersion()));
    }

    @Override
    public FoliaScheduler scheduler() {
        return scheduler;
    }

    @Override
    public PlatformType type() {
        return PlatformType.FOLIA;
    }

}
