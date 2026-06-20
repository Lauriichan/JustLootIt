package me.lauriichan.spigot.justlootit.platform;

import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.scheduler.Scheduler;
import me.lauriichan.spigot.justlootit.platform.version.ServerVersion;

public abstract class JustLootItPlatform {

    protected final Plugin plugin;
    protected final ISimpleLogger logger;
    protected final ServerVersion version;

    public JustLootItPlatform(Plugin plugin, ISimpleLogger logger) {
        this.plugin = plugin;
        this.logger = logger;
        this.version = detectVersion();
    }

    public final Plugin plugin() {
        return plugin;
    }

    public final ISimpleLogger logger() {
        return logger;
    }

    public final ServerVersion version() {
        return version;
    }

    public abstract PlatformType type();

    protected abstract ServerVersion detectVersion();

    public abstract Scheduler scheduler();

}
