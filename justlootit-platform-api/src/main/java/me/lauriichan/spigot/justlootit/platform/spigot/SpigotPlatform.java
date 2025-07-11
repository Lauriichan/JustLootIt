package me.lauriichan.spigot.justlootit.platform.spigot;

import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.IVersion;
import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;

public final class SpigotPlatform extends JustLootItPlatform {

    private final SpigotScheduler scheduler;

    public SpigotPlatform(Plugin plugin, ISimpleLogger logger) {
        super(plugin, logger);
        this.scheduler = new SpigotScheduler(plugin, logger);
    }

    @Override
    public IVersion version() {
        return SpigotVersion.INSTANCE;
    }
    
    @Override
    public SpigotScheduler scheduler() {
        return scheduler;
    }
    
    @Override
    public boolean isPaper() {
        return false;
    }

}
