package me.lauriichan.spigot.justlootit.nms.v1_20_R2.convert;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionAdapter;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;

public final class ConversionAdapter1_20_R2 extends ConversionAdapter {

    private final ISimpleLogger logger;

    private ExecutorService executor;

    public ConversionAdapter1_20_R2(ISimpleLogger logger) {
        this.logger = logger;
    }

    private ExecutorService executor() {
        if (executor != null) {
            return executor;
        }
        return executor = Executors.newCachedThreadPool();
    }

    @Override
    public ProtoWorld getWorld(File directory) {
        if (!directory.exists() || directory.isFile()) {
            return null;
        }
        File file = new File(directory, "level.dat");
        if (!file.exists()) {
            return null;
        }
        LevelStorageAccess session;
        ResourceKey<LevelStem> dimensionKey = findKey(directory);
        try {
            session = LevelStorageSource.createDefault(directory.getParentFile().toPath()).validateAndCreateAccess(directory.getName(),
                dimensionKey);
        } catch (IOException | ContentValidationException e) {
            return null;
        }
        LevelSummary info = session.getSummary();
        if (info != null && (info.requiresManualConversion() || !info.isCompatible())) {
            return null;
        }
        return new ProtoWorld1_20_R2(executor(), logger, new ChunkStorage(null, DataFixers.getDataFixer(), false), session, dimensionKey);
    }

    private ResourceKey<LevelStem> findKey(File directory) {
        File file = new File(directory, "DIM-1");
        if (file.exists()) {
            return LevelStem.NETHER;
        }
        file = new File(directory, "DIM1");
        if (file.exists()) {
            return LevelStem.END;
        }
        return LevelStem.OVERWORLD;
    }

}
