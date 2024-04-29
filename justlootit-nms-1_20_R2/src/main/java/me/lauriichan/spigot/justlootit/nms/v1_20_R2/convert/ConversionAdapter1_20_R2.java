package me.lauriichan.spigot.justlootit.nms.v1_20_R2.convert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.bukkit.craftbukkit.v1_20_R2.persistence.CraftPersistentDataTypeRegistry;

import com.mojang.datafixers.util.Pair;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionAdapter;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.VersionHandler1_20_R2;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.util.NmsHelper1_20_R2;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.validation.ContentValidationException;

public final class ConversionAdapter1_20_R2 extends ConversionAdapter {

    static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = NmsHelper1_20_R2.dataTypeRegistry();

    private final ISimpleLogger logger;
    private final VersionHandler1_20_R2 handler;

    public ConversionAdapter1_20_R2(VersionHandler1_20_R2 handler) {
        this.logger = handler.logger();
        this.handler = handler;
    }

    @Override
    public ProtoWorld1_20_R2 getWorld(File directory) {
        if (!directory.exists() || directory.isFile()) {
            return null;
        }
        File file = new File(directory, "level.dat");
        if (!file.exists()) {
            return null;
        }
        MinecraftServer server = NmsHelper1_20_R2.getServer();
        LevelStorageAccess session = server.storageSource;
        boolean closeSession = false;
        ResourceKey<LevelStem> dimensionKey = findKey(directory);
        Path path = directory.getParentFile().toPath();
        if (!path.equals(session.levelDirectory.path())) {
            try {
                session = LevelStorageSource.createDefault(path).validateAndCreateAccess(directory.getName(), dimensionKey);
                closeSession = true;
            } catch (IOException | ContentValidationException e) {
                return null;
            }
        }
        LevelSummary info = session.getSummary();
        try {
            if (info != null && (info.requiresManualConversion() || !info.isCompatible())) {
                session.close();
                return null;
            }
        } catch (IOException exp) {
            // Ignore cause we're just closing :)
            return null;
        }
        WorldLoader.DataLoadContext context = server.worldLoader;
        Pair<WorldData, WorldDimensions.Complete> pair = session.getDataTag(RegistryOps.create(NbtOps.INSTANCE, context.datapackWorldgen()),
            context.dataConfiguration(), context.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM),
            context.datapackWorldgen().allRegistriesLifecycle());
        return handler.applyCapabilities(new ProtoWorld1_20_R2(workerPool(), logger, new ChunkStorage(null, DataFixers.getDataFixer(), false),
            session, closeSession, dimensionKey, pair.getFirst()));
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
