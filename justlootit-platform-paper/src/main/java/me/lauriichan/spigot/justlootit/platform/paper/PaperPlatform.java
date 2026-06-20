package me.lauriichan.spigot.justlootit.platform.paper;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;
import me.lauriichan.spigot.justlootit.platform.PlatformType;
import me.lauriichan.spigot.justlootit.platform.spigot.SpigotScheduler;
import me.lauriichan.spigot.justlootit.platform.version.ServerVersion;
import me.lauriichan.spigot.justlootit.platform.version.SimpleVersion;

public final class PaperPlatform extends JustLootItPlatform {

    private final SpigotScheduler scheduler;

    public PaperPlatform(Plugin plugin, ISimpleLogger logger) {
        super(plugin, logger);
        this.scheduler = new SpigotScheduler(plugin, logger);
    }

    @Override
    protected ServerVersion detectVersion() {
        return new ServerVersion(Bukkit.getServer().getClass().getPackage().getName() + ".%s",
            SimpleVersion.of(Bukkit.getServer().getMinecraftVersion()));
    }

    @Override
    public SpigotScheduler scheduler() {
        return scheduler;
    }

    @Override
    public PlatformType type() {
        return PlatformType.PAPER;
    }

}
