package me.lauriichan.spigot.justlootit.version;

import java.io.File;
import java.io.IOException;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.nms.util.IOUtil;
import me.lauriichan.spigot.justlootit.platform.PlatformType;
import me.lauriichan.spigot.justlootit.platform.version.SimpleVersion;

@Extension
public final class Fix1_21_11To26_1WorldFormat extends VersionMigration {

    public Fix1_21_11To26_1WorldFormat() {
        super(0, "Moving JLI data to new world folders before paper deletes the data");
    }

    @Override
    public boolean applies(PlatformType platformType, SimpleVersion last, SimpleVersion current) {
        return platformType != PlatformType.SPIGOT && last.major() == 1 && current.major() >= 26;
    }

    @Override
    public void run(ISimpleLogger logger, JustLootItPlugin plugin) {
        File globalData = plugin.versionHelper().globalDataFolder();
        File dimensionsFolder = new File(globalData.getParentFile(), "dimensions/minecraft");
        String levelBaseName = globalData.getParentFile().getName();
        for (File worldFolder : globalData.getParentFile().getParentFile().listFiles()) {
            if (!worldFolder.isDirectory()) {
                continue;
            }
            File levelDat = new File(worldFolder, "level.dat");
            if (!levelDat.exists()) {
                continue;
            }
            File containerFolder = new File(worldFolder, StorageCapability.CONTAINER_PATH);
            if (!containerFolder.exists()) {
                continue;
            }
            String worldName = worldFolder.getName();
            if (worldName.startsWith(levelBaseName)) {
                if (worldName.equals(levelBaseName)) {
                    worldName = "overworld";
                } else {
                    worldName = worldName.substring(levelBaseName.length() + 1);
                    if (worldName.equals("nether")) {
                        worldName = "the_nether";
                    } else if (worldName.equals("the_end")) {
                        worldName = "the_end";
                    }
                }
            }
            File newContainerFolder = new File(dimensionsFolder, worldName.toLowerCase() + "/data/" + StorageCapability.CONTAINER_PATH);
            newContainerFolder.mkdirs();
            logger.info("Moving container files from '{0}' to '{1}'", containerFolder.getAbsolutePath(),
                newContainerFolder.getAbsolutePath());
            try {
                IOUtil.move(containerFolder.toPath(), newContainerFolder.toPath());
            } catch (IOException e) {
                logger.warning("Failed to move container files from '{0}' to '{1}'", e, containerFolder.getAbsolutePath(),
                    newContainerFolder.getAbsolutePath());
            }
        }
        File playerFolder = new File(globalData.getParentFile(), StorageCapability.PLAYER_PATH);
        if (playerFolder.exists()) {
            File newPlayerFolder = new File(globalData, StorageCapability.PLAYER_PATH);
            newPlayerFolder.mkdirs();
            logger.info("Moving player files from '{0}' to '{1}'", playerFolder.getAbsolutePath(), newPlayerFolder.getAbsolutePath());
            try {
                IOUtil.move(playerFolder.toPath(), newPlayerFolder.toPath());
            } catch (IOException e) {
                logger.warning("Failed to move player files from '{0}' to '{1}'", e, playerFolder.getAbsolutePath(),
                    newPlayerFolder.getAbsolutePath());
            }
        }
    }

}
