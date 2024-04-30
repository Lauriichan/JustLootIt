package me.lauriichan.spigot.justlootit.platform.paper;

import org.bukkit.plugin.Plugin;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.platform.IVersion;
import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;
import me.lauriichan.spigot.justlootit.platform.spigot.SpigotScheduler;

public final class PaperPlatform extends JustLootItPlatform {

    private final SpigotScheduler scheduler;

    public PaperPlatform(Plugin plugin, ISimpleLogger logger) {
        super(plugin, logger);
        this.scheduler = new SpigotScheduler(plugin, logger);
    }

    @Override
    public IVersion version() {
        return PaperVersion.INSTANCE;
    }
    
    @Override
    public SpigotScheduler scheduler() {
        return scheduler;
    }

}
