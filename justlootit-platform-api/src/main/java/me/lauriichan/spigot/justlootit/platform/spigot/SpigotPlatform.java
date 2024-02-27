package me.lauriichan.spigot.justlootit.platform.spigot;

import org.bukkit.plugin.Plugin;

import me.lauriichan.spigot.justlootit.platform.JustLootItPlatform;

public final class SpigotPlatform extends JustLootItPlatform {

    private final SpigotScheduler scheduler;

    public SpigotPlatform(Plugin plugin) {
        super(plugin);
        this.scheduler = new SpigotScheduler(plugin);
    }

    @Override
    public SpigotScheduler scheduler() {
        return scheduler;
    }

}
