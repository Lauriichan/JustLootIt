package me.lauriichan.spigot.justlootit.nms.paper.v26_1.convert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import org.bukkit.craftbukkit.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.persistence.PersistentDataType;

import com.mojang.serialization.Dynamic;

import io.papermc.paper.world.saveddata.PaperWorldPDC;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionAdapter;
import me.lauriichan.spigot.justlootit.nms.paper.v26_1.VersionHandler26_1;
import me.lauriichan.spigot.justlootit.nms.paper.v26_1.util.NmsHelper26_1;
import me.lauriichan.spigot.justlootit.nms.util.IOUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelDataAndDimensions.WorldDataAndGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.validation.ContentValidationException;

public final class ConversionAdapter26_1 extends ConversionAdapter {

    static final CraftPersistentDataTypeRegistry DATA_TYPE_REGISTRY = NmsHelper26_1.dataTypeRegistry();

    private final ISimpleLogger logger;
    private final VersionHandler26_1 handler;

    public ConversionAdapter26_1(VersionHandler26_1 handler) {
        this.logger = handler.logger();
        this.handler = handler;
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

        if (!directory.toPath().toAbsolutePath().equals(session.getLevelDirectory().path().normalize().toAbsolutePath())) {
            try {
                session = session.parent().validateAndCreateAccess(directory.getName());
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
        Path dimensionsPath = session.getLevelDirectory().path().resolve("dimensions");
        Iterator<Path> namespaceIter;
        try {
            namespaceIter = IOUtil.list(dimensionsPath);
        } catch (IOException e) {
            logger.debug(e);
            return ObjectList.of();
        }
        WorldLoader.DataLoadContext context = server.worldLoaderContext;
        ObjectArrayList<ProtoWorld26_1> worlds = new ObjectArrayList<>();
        while (namespaceIter.hasNext()) {
            Path namespacePath = namespaceIter.next();
            if (!Files.isDirectory(namespacePath)) {
                continue;
            }
            String namespaceStr = namespacePath.getFileName().toString();
            if (!Identifier.isValidNamespace(namespaceStr)) {
                continue;
            }
            Iterator<Path> idIter;
            try {
                idIter = IOUtil.list(namespacePath);
            } catch (IOException e) {
                logger.debug(e);
                continue;
            }
            while (idIter.hasNext()) {
                Path idPath = idIter.next();
                if (!Files.isDirectory(idPath)) {
                    continue;
                }
                Identifier levelStemId = Identifier.tryBuild(namespaceStr, idPath.getFileName().toString());
                if (levelStemId == null) {
                    continue;
                }
                PaperWorldPDC pdc;
                try (SavedDataStorage tmpStorage = new SavedDataStorage(idPath.resolve(LevelResource.DATA.id()), DataFixers.getDataFixer(),
                    server.registryAccess())) {
                    pdc = tmpStorage.get(PaperWorldPDC.TYPE);
                }
                if (pdc == null) {
                    continue;
                }
                String dimensionTypeIdRaw = pdc.persistentData().getOrDefault(handler.serviceProvider().dimensionTypeKey(),
                    PersistentDataType.STRING, "");
                if (dimensionTypeIdRaw.isEmpty()) {
                    continue;
                }
                Identifier dimensionTypeId = Identifier.tryParse(dimensionTypeIdRaw);
                if (dimensionTypeId == null) {
                    continue;
                }
                ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, levelStemId);
                WorldGenSettings worldGenSettings = LevelStorageSource
                    .readExistingSavedData(session, worldKey, context.datapackWorldgen(), WorldGenSettings.TYPE).result().orElse(null);
                if (worldGenSettings == null) {
                    continue;
                }
                WorldDataAndGenSettings worldData = new WorldDataAndGenSettings(server.getWorldData(), worldGenSettings);
                worlds.add(new ProtoWorld26_1(server, logger, session, closeSession, worldKey,
                    ResourceKey.create(Registries.LEVEL_STEM, dimensionTypeId), worldData));
            }
        }
        return ObjectLists.unmodifiable(worlds);
    }

}
