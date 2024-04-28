package me.lauriichan.spigot.justlootit.nms.v1_20_R3.convert;

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
import me.lauriichan.spigot.justlootit.nms.v1_20_R3.util.NmsHelper1_20_R3;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainer.Strategy;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class ProtoWorld1_20_R3 extends ProtoWorld implements LevelHeightAccessor {

    private final ExecutorService executor;
    private final ISimpleLogger logger;

    private final LevelStorageAccess session;

    // Don't use for access, only upgrade
    private final ChunkStorage chunkStorage;

    private final Registry<Biome> biomeRegistry;

    private final ResourceKey<LevelStem> dimensionKey;
    private final DimensionType dimensionType;

    private final WorldData worldData;

    private final ResourceKey<Level> worldKey;
    private final File worldFolder;
    private final Path regionPath;

    public ProtoWorld1_20_R3(final ExecutorService executor, final ISimpleLogger logger, final ChunkStorage chunkStorage,
        final LevelStorageAccess session, final ResourceKey<LevelStem> dimensionKey, WorldData worldData) {
        Frozen registry = NmsHelper1_20_R3.getServer().registryAccess();
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
        this.worldData = worldData;
        this.session = session;
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
                if (Files.isDirectory(path)) {
                    return;
                }
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
        try (RegionFile file = new RegionFile(path, regionPath, false)) {
            for (int x = 0; x < 32; x++) {
                for (int z = 0; z < 32; z++) {
                    try {
                        ChunkPos posInRegion = new ChunkPos(x, z);
                        if (!file.doesChunkExist(posInRegion)) {
                            continue;
                        }
                        CompoundTag tag;
                        try (DataInputStream input = file.getChunkDataInputStream(posInRegion)) {
                            if (input == null) {
                                continue;
                            }
                            tag = NbtIo.read(input);
                            if (tag == null || !tag.contains("Status", 8)) {
                                continue;
                            }
                            tag = chunkStorage.upgradeChunkTag(dimensionKey, () -> null, tag, Optional.empty(), posInRegion, null);
                        }
                        ListTag listTag = tag.getList("sections", 10);
                        LevelChunkSection[] sections = new LevelChunkSection[getSectionsCount()];
                        for (int i = 0; i < listTag.size(); i++) {
                            CompoundTag sectionTag = listTag.getCompound(i);
                            if (!sectionTag.contains("block_states", 10)) {
                                sections[i] = new LevelChunkSection(
                                    new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(),
                                        Strategy.SECTION_STATES),
                                    new PalettedContainer<>(biomeRegistry.asHolderIdMap(), biomeRegistry.getHolderOrThrow(Biomes.PLAINS),
                                        Strategy.SECTION_BIOMES));
                                continue;
                            }
                            sections[i] = new LevelChunkSection(ChunkSerializer.BLOCK_STATE_CODEC
                                .parse(NbtOps.INSTANCE, sectionTag.getCompound("block_states")).promotePartial((sx) -> {
                                    logger.warning("Something went wrong when reading chunk section: " + sx);
                                }).getOrThrow(false, (sx) -> {
                                    logger.warning("Something went wrong when reading chunk section: " + sx);
                                }), new PalettedContainer<>(biomeRegistry.asHolderIdMap(), biomeRegistry.getHolderOrThrow(Biomes.PLAINS),
                                    Strategy.SECTION_BIOMES));
                        }
                        int cx = tag.getInt("xPos"), cz = tag.getInt("zPos");
                        net.minecraft.world.level.chunk.ProtoChunk chunk = new net.minecraft.world.level.chunk.ProtoChunk(
                            new ChunkPos(cx, cz), null, sections, null, null, this, biomeRegistry, null);
                        if (tag.get("ChunkBukkitValues") instanceof CompoundTag persistentDataTag) {
                            chunk.persistentDataContainer.putAll(persistentDataTag);
                        }
                        ListTag entityListTag = tag.getList("entities", 10);
                        for (int i = 0; i < listTag.size(); i++) {
                            chunk.addEntity(entityListTag.getCompound(i));
                        }
                        Object2IntArrayMap<BlockPos> blockEntityMap = new Object2IntArrayMap<>();
                        blockEntityMap.defaultReturnValue(-1);
                        ListTag blockEntityListTag = tag.getList("block_entities", 10);
                        for (int i = 0; i < listTag.size(); i++) {
                            CompoundTag blockEntityTag = blockEntityListTag.getCompound(i);
                            chunk.setBlockEntityNbt(blockEntityTag);
                            blockEntityMap.put(BlockEntity.getPosFromTag(blockEntityTag), i);
                        }
                        ProtoChunk1_20_R3 protoChunk = new ProtoChunk1_20_R3(this, chunk, cx, cz);
                        consumer.accept(protoChunk);
                        if (protoChunk.isDirty()) {
                            blockEntityListTag.clear();
                            for (ProtoBlockEntity rawBlock : protoChunk.getBlockEntities()) {
                                blockEntityListTag.add(((ProtoBlockEntity1_20_R3) rawBlock).tag());
                            }
                            for (int i = 0; i < listTag.size(); i++) {
                                LevelChunkSection section = sections[i];
                                CompoundTag sectionTag = listTag.getCompound(i);
                                if (!sectionTag.contains("block_states", 10)) {
                                    continue;
                                }
                                sectionTag.put("block_states", ChunkSerializer.BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, section.getStates()).getOrThrow(false, (sx) -> {
                                    logger.warning("Something went wrong when writing chunk section: " + sx);
                                }));
                            }
                            try (DataOutputStream output = file.getChunkDataOutputStream(posInRegion)) {
                                NbtIo.write(tag, output);
                            }
                        }
                    } finally {
                        counter.increment();
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            // Maybe mark as complete? Unsure tho
            counter.increment(counter.max() - counter.current());
            logger.error("Failed to convert region '{1}' in level '{0}'!", e, worldData.getLevelName(), path.getFileName().toString());
        }
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
