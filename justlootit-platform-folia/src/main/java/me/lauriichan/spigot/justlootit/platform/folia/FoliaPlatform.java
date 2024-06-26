package me.lauriichan.spigot.justlootit.platform.folia;

import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.IVersion;
import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;

public final class FoliaPlatform extends JustLootItPlatform {

    private final FoliaScheduler scheduler;

    public FoliaPlatform(Plugin plugin, ISimpleLogger logger) {
        super(plugin, logger);
        this.scheduler = new FoliaScheduler(plugin, logger);
    }
    
    @Override
    public IVersion version() {
        return FoliaVersion.INSTANCE;
    }

    @Override
    public FoliaScheduler scheduler() {
        return scheduler;
    }

}
