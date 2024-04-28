package me.lauriichan.spigot.justlootit.nms.v1_20_R2.convert;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.persistence.CraftPersistentDataTypeRegistry;

import com.mojang.datafixers.util.Pair;

import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionAdapter;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.VersionHandler1_20_R2;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.util.NmsHelper1_20_R2;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
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

    static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY;

    static {
        DATA_TYPE_REGISTRY = (CraftPersistentDataTypeRegistry) JavaAccess.getStaticValue(CraftEntity.class, "DATA_TYPE_REGISTRY");
    }

    private final ISimpleLogger logger;
    private final VersionHandler1_20_R2 handler;

    private ExecutorService executor;

    public ConversionAdapter1_20_R2(VersionHandler1_20_R2 handler) {
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
    public ProtoWorld1_20_R2 getWorld(File directory) {
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
        @SuppressWarnings("resource")
        WorldLoader.DataLoadContext context = NmsHelper1_20_R2.getServer().worldLoader;
        Pair<WorldData, WorldDimensions.Complete> pair = session.getDataTag(RegistryOps.create(NbtOps.INSTANCE, context.datapackWorldgen()), context.dataConfiguration(), context.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM), context.datapackWorldgen().allRegistriesLifecycle());
        return handler.applyCapabilities(new ProtoWorld1_20_R2(executor(), logger, new ChunkStorage(null, DataFixers.getDataFixer(), false), session, dimensionKey, pair.getFirst()));
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
