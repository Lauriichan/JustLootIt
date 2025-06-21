package me.lauriichan.spigot.justlootit.config.world;

import java.util.UUID;

import org.bukkit.World;

import me.lauriichan.minecraft.pluginbase.config.IMultiConfigExtension;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;

@Extension
public final class WorldMultiConfig implements IMultiConfigExtension<UUID, World, WorldConfig> {

    private final boolean trialChamberBuggedVersion;

    public WorldMultiConfig(final JustLootItPlugin plugin) {
        this.trialChamberBuggedVersion = plugin.versionHelper().isTrialChamberBugged();
    }

    @Override
    public WorldConfig create() {
        return new WorldConfig(trialChamberBuggedVersion);
    }

    @Override
    public UUID getConfigKey(World element) {
        return element.getUID();
    }

    @Override
    public String path(World element) {
        return "data://worlds/%s/settings.json".formatted(element.getName());
    }

    @Override
    public Class<WorldConfig> type() {
        return WorldConfig.class;
    }

}
