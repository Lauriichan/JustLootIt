package me.lauriichan.spigot.justlootit.nms.v26_1.convert;

import java.io.File;
import java.io.IOException;

import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;

import com.mojang.serialization.Dynamic;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionAdapter;
import me.lauriichan.spigot.justlootit.nms.v26_1.VersionHandler26_1;
import me.lauriichan.spigot.justlootit.nms.v26_1.util.NmsHelper26_1;
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

public final class ConversionAdapter26_1 extends ConversionAdapter {

    static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = NmsHelper26_1.dataTypeRegistry();

    private final ISimpleLogger logger;

    public ConversionAdapter26_1(VersionHandler26_1 handler) {
        this.logger = handler.logger();
    }

    @Override
    public ObjectList<ProtoWorld26_1> getWorlds(File directory) {
        if (!directory.exists() || directory.isFile()) {
            return ObjectList.of();
        }
        File file = new File(directory, "level.dat");
        if (!file.exists()) {
            return ObjectList.of();
        }
        MinecraftServer server = NmsHelper26_1.getServer();
        LevelStorageAccess session = server.storageSource;
        boolean closeSession = false;
        ResourceKey<LevelStem> dimensionKey = findKey(directory);
        if (dimensionKey == null) {
            return ObjectList.of();
        }
        if (!directory.toPath().equals(session.getLevelDirectory().path())) {
            try {
                session = session.parent().validateAndCreateAccess(directory.getName(), dimensionKey);
                closeSession = true;
            } catch (IOException | ContentValidationException e) {
                return ObjectList.of();
            }
        }
        Dynamic<?> dynamic;
        LevelSummary info;
        try {
            if (!session.hasWorldData()) {
                if (closeSession) {
                    session.close();
                }
                return ObjectList.of();
            }
            try {
                dynamic = session.getUnfixedDataTag(false);
                info = session.fixAndGetSummaryFromTag(dynamic);
            } catch (NbtException | net.minecraft.nbt.ReportedNbtException | IOException exp) {
                try {
                    dynamic = session.getUnfixedDataTagWithFallback();
                    info = session.fixAndGetSummaryFromTag(dynamic);
                } catch (NbtException | net.minecraft.nbt.ReportedNbtException | IOException exp1) {
                    if (closeSession) {
                        session.close();
                    }
                    return ObjectList.of();
                }
                session.restoreLevelDataFromOld();
            }
            if (info.requiresManualConversion() || !info.isCompatible()) {
                if (closeSession) {
                    session.close();
                }
                return ObjectList.of();
            }
        } catch (IOException e) {
            // Ignore cause we're just closing :)
            return ObjectList.of();
        }
        WorldLoader.DataLoadContext context = server.worldLoader;
        LevelDataAndDimensions levelData = LevelStorageSource.getLevelDataAndDimensions(session, dynamic, context.dataConfiguration(),
            context.datapackDimensions().lookupOrThrow(Registries.LEVEL_STEM), context.datapackWorldgen());
        return ObjectList
            .of(new ProtoWorld26_1(workerPool(logger), logger, session, closeSession, dimensionKey, levelData.worldDataAndGenSettings()));
    }

    private ResourceKey<LevelStem> findKey(File directory) {
        File file = new File(directory, "dimensions/minecraft/overworld/region");
        if (file.exists()) {
            return LevelStem.OVERWORLD;
        }
        file = new File(directory, "dimensions/minecraft/the_nether/region");
        if (file.exists()) {
            return LevelStem.NETHER;
        }
        file = new File(directory, "dimensions/minecraft/the_end/region");
        if (file.exists()) {
            return LevelStem.END;
        }
        return null; // TODO: No clue, this has to be fixed
    }

}
