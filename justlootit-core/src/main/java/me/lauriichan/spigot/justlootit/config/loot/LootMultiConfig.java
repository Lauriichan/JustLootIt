package me.lauriichan.spigot.justlootit.config.loot;

import java.util.UUID;

import org.bukkit.World;

import me.lauriichan.minecraft.pluginbase.config.IMultiConfigExtension;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;

@Extension
public final class LootMultiConfig implements IMultiConfigExtension<UUID, World, LootConfig> {
    
    private final JustLootItPlugin plugin;
    
    public LootMultiConfig(JustLootItPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public LootConfig create() {
        return new LootConfig(plugin);
    }

    @Override
    public UUID getConfigKey(World element) {
        return element.getUID();
    }

    @Override
    public String path(World element) {
        return path(element.getName());
    }

    @Override
    public Class<LootConfig> type() {
        return LootConfig.class;
    }
    
    public static String path(String worldName) {
        return "data://worlds/%s/loottables.json".formatted(worldName);
    }

}
