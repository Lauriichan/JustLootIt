package me.lauriichan.spigot.justlootit.config.migration;

import me.lauriichan.minecraft.pluginbase.config.Configuration;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.config.ConfigMigrationExtension;
import me.lauriichan.spigot.justlootit.config.MainConfig;

@Extension
public final class MainConfigMigration_v1 extends ConfigMigrationExtension<MainConfig> {

    public MainConfigMigration_v1() {
        super(MainConfig.class, 0, 1);
    }

    @Override
    public String description() {
        return "Updating player cache config layout";
    }

    @Override
    public void migrate(Configuration config) throws Throwable {
        copy(config, "player.cache.size", "cache.player.size");
        copy(config, "player.cache.timeout-days", "cache.player.timeout-inventory-days");
        config.remove("player");
    }

    public final void copy(Configuration config, String pathA, String pathB) {
        if (!config.contains(pathA)) {
            return;
        }
        config.set(pathB, config.get(pathA));
    }

}
