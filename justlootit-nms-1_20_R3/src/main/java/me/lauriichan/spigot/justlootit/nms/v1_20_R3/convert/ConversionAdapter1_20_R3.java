package me.lauriichan.spigot.justlootit.nms.v1_20_R3.convert;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.craftbukkit.v1_20_R3.persistence.CraftPersistentDataTypeRegistry;

import com.mojang.serialization.Dynamic;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionAdapter;
import me.lauriichan.spigot.justlootit.nms.v1_20_R3.VersionHandler1_20_R3;
import me.lauriichan.spigot.justlootit.nms.v1_20_R3.util.NmsHelper1_20_R3;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;

public final class ConversionAdapter1_20_R3 extends ConversionAdapter {

    static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = NmsHelper1_20_R3.dataTypeRegistry();

    private final ISimpleLogger logger;
    private final VersionHandler1_20_R3 handler;

    private ExecutorService executor;

    public ConversionAdapter1_20_R3(VersionHandler1_20_R3 handler) {
        this.logger = handler.logger();
        this.handler = handler;
    }

    private ExecutorService executor() {
        if (executor != null) {
            return executor;
        }
        return executor = Executors.newCachedThreadPool();
    }

    @Override
    public ProtoWorld1_20_R3 getWorld(File directory) {
        if (!directory.exists() || directory.isFile()) {
            return null;
        }
        File file = new File(directory, "level.dat");
        if (!file.exists()) {
            return null;
        }
        MinecraftServer server = NmsHelper1_20_R3.getServer();
        LevelStorageAccess session = server.storageSource;
        boolean closeSession = false;
        ResourceKey<LevelStem> dimensionKey = findKey(directory);
        if (!directory.toPath().equals(session.getLevelDirectory().path())) {
            try {
                session = session.parent().validateAndCreateAccess(directory.getName(), dimensionKey);
                closeSession = true;
            } catch (IOException | ContentValidationException e) {
                System.out.println("STILL ACCESS???????");
                return null;
            }
        }
        Dynamic<?> dynamic;
        LevelSummary info;
        try {
            if (!session.hasWorldData()) {
                if (closeSession) {
                    session.close();
                } else {
                    System.out.println("NO WORLD DATA");
                }
                return null;
            }
            try {
                dynamic = session.getDataTag();
                info = session.getSummary(dynamic);
            } catch (NbtException | net.minecraft.nbt.ReportedNbtException | IOException exp) {
                try {
                    dynamic = session.getDataTagFallback();
                    info = session.getSummary(dynamic);
                } catch (NbtException | net.minecraft.nbt.ReportedNbtException | IOException exp1) {
                    if (closeSession) {
                        session.close();
                    } else {
                        System.out.println("NO TAG");
                    }
                    return null;
                }
                session.restoreLevelDataFromOld();
            }
            if (info.requiresManualConversion() || !info.isCompatible()) {
                if (closeSession) {
                    session.close();
                } else {
                    System.out.println("INCOMPATIBLE");
                }
                return null;
            }
        } catch (IOException e) {
            // Ignore cause we're just closing :)
            return null;
        }
        WorldLoader.DataLoadContext context = server.worldLoader;
        LevelDataAndDimensions levelData = LevelStorageSource.getLevelDataAndDimensions(dynamic, context.dataConfiguration(),
            context.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM), context.datapackWorldgen());
        return handler.applyCapabilities(new ProtoWorld1_20_R3(executor(), logger, new ChunkStorage(null, DataFixers.getDataFixer(), false),
            session, closeSession, dimensionKey, levelData.worldData()));
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
