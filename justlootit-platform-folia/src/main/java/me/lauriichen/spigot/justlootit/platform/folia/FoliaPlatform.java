package me.lauriichen.spigot.justlootit.platform.folia;

import org.bukkit.plugin.Plugin;

import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;

public final class FoliaPlatform extends JustLootItPlatform {

    private final FoliaScheduler scheduler;

    public FoliaPlatform(Plugin plugin) {
        super(plugin);
        this.scheduler = new FoliaScheduler(plugin);
    }

    @Override
    public FoliaScheduler scheduler() {
        return scheduler;
    }

}
