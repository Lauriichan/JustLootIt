package me.lauriichan.spigot.justlootit.nms.v1_21_R6.convert;

import java.io.File;
import java.io.IOException;

import org.bukkit.craftbukkit.v1_21_R6.persistence.CraftPersistentDataTypeRegistry;

import com.mojang.serialization.Dynamic;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionAdapter;
import me.lauriichan.spigot.justlootit.nms.v1_21_R6.VersionHandler1_21_R6;
import me.lauriichan.spigot.justlootit.nms.v1_21_R6.util.NmsHelper1_21_R6;
import me.lauriichan.spigot.justlootit.nms.v1_21_R6.util.PlatformHelper1_21_R6;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;

public final class ConversionAdapter1_21_R6 extends ConversionAdapter {

    static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = NmsHelper1_21_R6.dataTypeRegistry();

    private final ISimpleLogger logger;
    private final VersionHandler1_21_R6 handler;

    public ConversionAdapter1_21_R6(VersionHandler1_21_R6 handler) {
        this.logger = handler.logger();
        this.handler = handler;
    }

    @Override
    public ProtoWorld1_21_R6 getWorld(File directory) {
        if (!directory.exists() || directory.isFile()) {
            return null;
        }
        File file = new File(directory, "level.dat");
        if (!file.exists()) {
            return null;
        }
        MinecraftServer server = NmsHelper1_21_R6.getServer();
        LevelStorageAccess session = server.storageSource;
        boolean closeSession = false;
        ResourceKey<LevelStem> dimensionKey = findKey(directory);
        if (!directory.toPath().equals(session.getLevelDirectory().path())) {
            try {
                session = session.parent().validateAndCreateAccess(directory.getName(), dimensionKey);
                closeSession = true;
            } catch (IOException | ContentValidationException e) {
                return null;
            }
        }
        Dynamic<?> dynamic;
        LevelSummary info;
        try {
            if (!session.hasWorldData()) {
                if (closeSession) {
                    session.close();
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
                    }
                    return null;
                }
                session.restoreLevelDataFromOld();
            }
            if (info.requiresManualConversion() || !info.isCompatible()) {
                if (closeSession) {
                    session.close();
                }
                return null;
            }
        } catch (IOException e) {
            // Ignore cause we're just closing :)
            return null;
        }
        WorldLoader.DataLoadContext context = PlatformHelper1_21_R6.getLoadContext(server);
        LevelDataAndDimensions levelData = LevelStorageSource.getLevelDataAndDimensions(dynamic, context.dataConfiguration(),
            context.datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM), context.datapackWorldgen());
        return handler.applyCapabilities(new ProtoWorld1_21_R6(workerPool(logger), logger,
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
