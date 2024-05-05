package me.lauriichan.spigot.justlootit.config.world;

import java.util.UUID;

import org.bukkit.World;

import me.lauriichan.minecraft.pluginbase.config.IMultiConfigExtension;
import me.lauriichan.minecraft.pluginbase.extension.Extension;

@Extension
public final class WorldMultiConfig implements IMultiConfigExtension<UUID, World, WorldConfig> {

    @Override
    public WorldConfig create() {
        return new WorldConfig();
    }

    @Override
    public UUID getConfigKey(World element) {
        return element.getUID();
    }

    @Override
    public String path(World element) {
        return "data://worlds/%s/settings.json".formatted(element.getName());
    }

}
