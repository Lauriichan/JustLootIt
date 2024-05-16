package me.lauriichan.spigot.justlootit.nms.v1_20_R2.convert;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.nms.convert.ConversionProgress;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlockEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;
import me.lauriichan.spigot.justlootit.nms.util.counter.CompositeCounter;
import me.lauriichan.spigot.justlootit.nms.util.counter.Counter;
import me.lauriichan.spigot.justlootit.nms.util.counter.SimpleCounter;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.util.NmsHelper1_20_R2;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.util.PlatformHelper1_20_R2;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.ChunkStatus.ChunkType;
import net.minecraft.world.level.chunk.PalettedContainer.Strategy;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class ProtoWorld1_20_R2 extends ProtoWorld implements LevelHeightAccessor {

    private final ExecutorService executor;
    private final ISimpleLogger logger;

    private final LevelStorageAccess session;
    private final boolean closeSession;

    // Don't use for access, only upgrade
    private final ChunkStorage chunkStorage;

    private final Registry<Biome> biomeRegistry;

    private final ResourceKey<LevelStem> dimensionKey;
    private final DimensionType dimensionType;

    private final WorldData worldData;

    private final ResourceKey<Level> worldKey;
    private final File worldFolder;

    private final Path regionPath;
    private final Path entityPath;

    private final DataFixer fixerUpper = DataFixers.getDataFixer();

    public ProtoWorld1_20_R2(final ExecutorService executor, final ISimpleLogger logger, final ChunkStorage chunkStorage,
        final LevelStorageAccess session, final boolean closeSession, final ResourceKey<LevelStem> dimensionKey, WorldData worldData) {
        Frozen registry = NmsHelper1_20_R2.getServer().registryAccess();
        this.executor = executor;
        this.logger = logger;
        this.chunkStorage = chunkStorage;
        this.dimensionKey = dimensionKey;
        this.dimensionType = registry.registryOrThrow(Registries.LEVEL_STEM).get(dimensionKey).type().value();
        this.biomeRegistry = registry.registryOrThrow(Registries.BIOME);
        this.worldKey = ResourceKey.create(Registries.DIMENSION, dimensionKey.location());
        Path dimensionPath = session.getDimensionPath(worldKey);
        this.worldFolder = toWorldFolder(dimensionPath);
        this.regionPath = dimensionPath.resolve("region");
        this.entityPath = dimensionPath.resolve("entities");
        this.worldData = worldData;
        this.session = session;
        this.closeSession = closeSession;
        
        // Apply patches
        PlatformHelper1_20_R2.patchCraftBlockDataEnumValues();

        // Try to load various registries
        Blocks.AIR.getClass();
        EntityType.BAT.getClass();
        BlockEntityType.CHEST.getClass();
    }

    private File toWorldFolder(Path dimensionPath) {
        File folder = dimensionPath.toFile();
        Path levelDatPath = dimensionPath.resolve("level.dat");
        if (Files.exists(levelDatPath) && !Files.isDirectory(levelDatPath)) {
            return folder;
        }
        return folder.getParentFile();
    }

    @Override
    public void close() {
        super.close();
        if (!closeSession) {
            return;
        }
        try {
            session.close();
        } catch (IOException e) {
            logger.warning("Failed to close level access of level '{0}'", session.getLevelId());
        }
    }

    @Override
    public long getSeed() {
        return worldData.worldGenOptions().seed();
    }

    @Override
    public String getName() {
        return worldData.getLevelName();
    }

    @Override
    public File getWorldFolder() {
        return worldFolder;
    }

    @Override
    public ConversionProgress streamChunks(Consumer<ProtoChunk> consumer) {
        CompositeCounter compositeCounter = new CompositeCounter();
        // This should most likely block until we are done however this is also the one informing about the progress
        ObjectArrayList<CompletableFuture<Void>> futures = new ObjectArrayList<>();
        try {
            Files.list(regionPath).forEach(path -> {
                SimpleCounter counter = new SimpleCounter(1024);
                compositeCounter.add(counter);
                futures.add(CompletableFuture.runAsync(() -> streamRegion(path, counter, consumer), executor));
            });
        } catch (IOException e) {
            logger.error("Failed to convert level '{0}'!", e, worldData.getLevelName());
        }
        return new ConversionProgress(compositeCounter, futures);
    }

    private void streamRegion(Path path, Counter counter, Consumer<ProtoChunk> consumer) {
        int minSection = getMinSection();
        int maxSection = getMaxSection();
        int sectionCount = getSectionsCount();
        Path entityFilePath = entityPath.resolve(path.getFileName());
        try (RegionFile chunkRegion = new RegionFile(path, regionPath, false)) {
            RegionFile entityRegion = null;
            if (Files.exists(entityPath)) {
                entityRegion = new RegionFile(entityFilePath, entityPath, false);
            }
            try {
                for (int x = 0; x < 32; x++) {
                    for (int z = 0; z < 32; z++) {
                        try {
                            ChunkPos posInRegion = new ChunkPos(x, z);
                            CompoundTag chunkTag = readRegionTag(chunkRegion, posInRegion);
                            if (chunkTag == null) {
                                continue;
                            }
                            ListTag listTag = chunkTag.getList("sections", 10);
                            LevelChunkSection[] sections = new LevelChunkSection[sectionCount];
                            int sectionIndex = 0;
                            int sectionOffset = 0;
                            int chunkSectionCount = 0;
                            int chunkY = chunkTag.getInt("yPos");
                            if (chunkY != minSection) {
                                sectionOffset = -chunkY;
                                for (int i = 0; i < sectionOffset; i++) {
                                    sections[i] = new LevelChunkSection(
                                        new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(),
                                            Strategy.SECTION_STATES),
                                        new PalettedContainer<>(biomeRegistry.asHolderIdMap(),
                                            biomeRegistry.getHolderOrThrow(Biomes.PLAINS), Strategy.SECTION_BIOMES));
                                }
                            }
                            for (int i = 0; i < listTag.size(); i++) {
                                CompoundTag sectionTag = listTag.getCompound(i);
                                byte y = sectionTag.getByte("Y");
                                if (y < minSection || y > maxSection) {
                                    continue;
                                }
                                chunkSectionCount++;
                                if (!sectionTag.contains("block_states", 10)) {
                                    sections[sectionIndex++] = new LevelChunkSection(
                                        new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(),
                                            Strategy.SECTION_STATES),
                                        new PalettedContainer<>(biomeRegistry.asHolderIdMap(),
                                            biomeRegistry.getHolderOrThrow(Biomes.PLAINS), Strategy.SECTION_BIOMES));
                                    continue;
                                }
                                sections[sectionIndex++] = new LevelChunkSection(ChunkSerializer.BLOCK_STATE_CODEC
                                    .parse(NbtOps.INSTANCE, sectionTag.getCompound("block_states")).promotePartial((sx) -> {
                                        logger.warning("Something went wrong when reading chunk section: " + sx);
                                    }).getOrThrow(false, (sx) -> {
                                        logger.warning("Something went wrong when reading chunk section: " + sx);
                                    }), new PalettedContainer<>(biomeRegistry.asHolderIdMap(),
                                        biomeRegistry.getHolderOrThrow(Biomes.PLAINS), Strategy.SECTION_BIOMES));
                            }
                            if (chunkSectionCount != sectionCount) {
                                for (int i = chunkSectionCount; i < sectionCount; i++) {
                                    sections[i] = new LevelChunkSection(
                                        new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(),
                                            Strategy.SECTION_STATES),
                                        new PalettedContainer<>(biomeRegistry.asHolderIdMap(),
                                            biomeRegistry.getHolderOrThrow(Biomes.PLAINS), Strategy.SECTION_BIOMES));
                                }
                            }
                            int cx = chunkTag.getInt("xPos"), cz = chunkTag.getInt("zPos");
                            net.minecraft.world.level.chunk.ProtoChunk chunk = new net.minecraft.world.level.chunk.ProtoChunk(
                                new ChunkPos(cx, cz), null, sections, null, null, this, biomeRegistry, null);
                            if (chunkTag.get("ChunkBukkitValues") instanceof CompoundTag persistentDataTag) {
                                chunk.persistentDataContainer.putAll(persistentDataTag);
                            }
                            Pair<CompoundTag, String> entityTag = readEntityTag(chunkTag, entityRegion, posInRegion);
                            if (entityTag != null) {
                                ListTag entityListTag = entityTag.getFirst().getList(entityTag.getSecond(), 10);
                                for (int i = 0; i < entityListTag.size(); i++) {
                                    chunk.addEntity(entityListTag.getCompound(i));
                                }
                            }
                            Object2IntArrayMap<BlockPos> blockEntityMap = new Object2IntArrayMap<>();
                            ListTag blockEntityListTag = chunkTag.getList("block_entities", 10);
                            for (int i = 0; i < blockEntityListTag.size(); i++) {
                                CompoundTag blockEntityTag = blockEntityListTag.getCompound(i);
                                chunk.setBlockEntityNbt(blockEntityTag);
                                blockEntityMap.put(BlockEntity.getPosFromTag(blockEntityTag), i);
                            }
                            ProtoChunk1_20_R2 protoChunk = new ProtoChunk1_20_R2(this, chunk, cx, cz);
                            consumer.accept(protoChunk);
                            if (protoChunk.isDirty()) {
                                blockEntityListTag.clear();
                                for (ProtoBlockEntity rawBlock : protoChunk.getBlockEntities()) {
                                    blockEntityListTag.add(((ProtoBlockEntity1_20_R2) rawBlock).tag());
                                }
                                sectionIndex = 0;
                                for (int i = 0; i < listTag.size(); i++) {
                                    CompoundTag sectionTag = listTag.getCompound(i);
                                    byte y = sectionTag.getByte("Y");
                                    if (y < minSection || y > maxSection) {
                                        continue;
                                    }
                                    if (!sectionTag.contains("block_states", 10)) {
                                        sectionIndex++;
                                        continue;
                                    }
                                    sectionTag.put("block_states", ChunkSerializer.BLOCK_STATE_CODEC
                                        .encodeStart(NbtOps.INSTANCE, sections[sectionOffset + sectionIndex++].getStates()).getOrThrow(false, (sx) -> {
                                            logger.warning("Something went wrong when writing chunk section: " + sx);
                                        }));
                                }
                                try (DataOutputStream output = chunkRegion.getChunkDataOutputStream(posInRegion)) {
                                    NbtIo.write(chunkTag, output);
                                }
                                if (entityTag != null && entityTag.getFirst() != chunkTag) {
                                    try (DataOutputStream output = entityRegion.getChunkDataOutputStream(posInRegion)) {
                                        NbtIo.write(entityTag.getFirst(), output);
                                    }
                                }
                            }
                        } finally {
                            counter.increment();
                        }
                    }
                }
            } finally {
                if (entityRegion != null) {
                    entityRegion.close();
                }
            }
        } catch (IOException | RuntimeException e) {
            // Maybe mark as complete? Unsure tho
            counter.increment(counter.max() - counter.current());
            logger.error("Failed to convert region '{1}' in level '{0}'!", e, worldData.getLevelName(), path.getFileName().toString());
        }
    }

    private CompoundTag readRegionTag(RegionFile file, ChunkPos pos) throws IOException {
        if (!file.doesChunkExist(pos)) {
            return null;
        }
        CompoundTag tag;
        try (DataInputStream stream = file.getChunkDataInputStream(pos)) {
            if (stream == null) {
                return null;
            }
            tag = NbtIo.read(stream);
        }
        if (tag == null || !tag.contains("Status", 8)) {
            return null;
        }
        return chunkStorage.upgradeChunkTag(dimensionKey, () -> null, tag, Optional.empty(), pos, null);
    }

    private Pair<CompoundTag, String> readEntityTag(CompoundTag chunkTag, RegionFile file, ChunkPos pos) throws IOException {
        ChunkStatus status = ChunkStatus.byName(chunkTag.getString("Status"));
        if (status.getChunkType() == ChunkType.PROTOCHUNK) {
            return Pair.of(chunkTag, "entities");
        }
        if (!file.doesChunkExist(pos)) {
            return null;
        }
        CompoundTag tag;
        try (DataInputStream stream = file.getChunkDataInputStream(pos)) {
            if (stream == null) {
                return null;
            }
            tag = NbtIo.read(stream);
        }
        if (tag == null) {
            return null;
        }
        return Pair.of(DataFixTypes.ENTITY_CHUNK.updateToCurrentVersion(fixerUpper, tag, NbtUtils.getDataVersion(tag, -1)), "Entities");
    }

    @Override
    public int getHeight() {
        return dimensionType.height();
    }

    @Override
    public int getMinBuildHeight() {
        return dimensionType.minY();
    }

}
