package me.lauriichan.spigot.justlootit.nms.v1_21_R6.convert;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.logger.ISimpleLogger;
import me.lauriichan.spigot.justlootit.nms.convert.ConvThread;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoBlockEntity;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoWorld;
import me.lauriichan.spigot.justlootit.storage.util.counter.CompositeCounter;
import me.lauriichan.spigot.justlootit.storage.util.counter.Counter;
import me.lauriichan.spigot.justlootit.storage.util.counter.CounterProgress;
import me.lauriichan.spigot.justlootit.storage.util.counter.SimpleCounter;
import me.lauriichan.spigot.justlootit.nms.v1_21_R6.util.NmsHelper1_21_R6;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

public class ProtoWorld1_21_R6 extends ProtoWorld implements LevelHeightAccessor {

    // This is not relevant for Spigot therefore we can just put garbage in there that we can cache
    // The functionality won't change on Paper either, it just fixes an NPE
    private static final Path PAPER_CHUNK_STORAGE_PATH_FIX = Paths.get("path");

    private final ISimpleLogger logger;

    private final LevelStorageAccess session;
    private final boolean closeSession;

    // Don't use for access, only upgrade
    private final ChunkStorage chunkStorage;

    private final ResourceKey<LevelStem> dimensionKey;
    private final DimensionType dimensionType;
    
    private final PalettedContainerFactory containerFactory;

    private final WorldData worldData;

    private final ResourceKey<Level> worldKey;
    private final File worldFolder;

    private final Path regionPath;
    private final Path entityPath;

    private final DataFixer fixerUpper = DataFixers.getDataFixer();

    private final RegionStorageInfo regionInfo, entityInfo;

    private final Frozen registry;

    public ProtoWorld1_21_R6(final Executor executor, final ISimpleLogger logger, final LevelStorageAccess session,
        final boolean closeSession, final ResourceKey<LevelStem> dimensionKey, WorldData worldData) {
        super(executor);
        this.registry = NmsHelper1_21_R6.getServer().registryAccess();
        this.logger = logger;
        this.dimensionKey = dimensionKey;
        this.dimensionType = registry.lookupOrThrow(Registries.LEVEL_STEM).getValue(dimensionKey).type().value();
        this.worldKey = ResourceKey.create(Registries.DIMENSION, dimensionKey.location());
        Path dimensionPath = session.getDimensionPath(worldKey);
        this.worldFolder = toWorldFolder(dimensionPath);
        this.regionPath = dimensionPath.resolve("region");
        this.entityPath = dimensionPath.resolve("entities");
        this.worldData = worldData;
        this.session = session;
        this.closeSession = closeSession;
        this.regionInfo = new RegionStorageInfo(session.getLevelId(), worldKey, "region");
        this.entityInfo = new RegionStorageInfo(session.getLevelId(), worldKey, "entities");
        this.chunkStorage = new ChunkStorage(regionInfo, PAPER_CHUNK_STORAGE_PATH_FIX, DataFixers.getDataFixer(), false);
        this.containerFactory = PalettedContainerFactory.create(registry);

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
    public CounterProgress streamChunks(Consumer<ProtoChunk> consumer) {
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
        } catch (NoSuchFileException ignore) {
        } catch (IOException e) {
            logger.error("Failed to convert level '{0}'!", e, worldData.getLevelName());
        }
        return new CounterProgress(compositeCounter, futures);
    }

    private void streamRegion(Path path, Counter counter, Consumer<ProtoChunk> consumer) {
        ConvThread thread = (ConvThread) Thread.currentThread();
        int minSection = getMinSectionY();
        int maxSection = getMaxSectionY();
        int sectionCount = getSectionsCount();
        Path fileName = path.getFileName();
        Path entityFilePath = entityPath.resolve(fileName);
        thread.setRegion(fileName.toString());
        thread.setTask("");
        try (RegionFile chunkRegion = new RegionFile(regionInfo, path, regionPath, false)) {
            RegionFile entityRegion = null;
            if (Files.exists(entityPath)) {
                entityRegion = new RegionFile(entityInfo, entityFilePath, entityPath, false);
            }
            try {
                for (int x = 0; x < 32; x++) {
                    for (int z = 0; z < 32; z++) {
                        thread.setChunk(x, z);
                        thread.setTask("Reading chunk");
                        try {
                            ChunkPos posInRegion = new ChunkPos(x, z);
                            CompoundTag chunkTag = readRegionTag(chunkRegion, posInRegion);
                            if (chunkTag == null) {
                                continue;
                            }
                            ListTag listTag = chunkTag.getList("sections").filter(list -> list.identifyRawElementType() == 10)
                                .orElseGet(ListTag::new);
                            LevelChunkSection[] sections = new LevelChunkSection[sectionCount];
                            int sectionIndex = 0;
                            int sectionOffset = 0;
                            int chunkSectionCount = 0;
                            int chunkY = chunkTag.getIntOr("yPos", 0);
                            if (chunkY != minSection) {
                                sectionOffset = -chunkY;
                                for (int i = 0; i < sectionOffset; i++) {
                                    sections[i] = new LevelChunkSection(
                                        containerFactory.createForBlockStates(),
                                        containerFactory.createForBiomes());
                                }
                            }
                            thread.setTask("Reading sections");
                            for (int i = 0; i < listTag.size(); i++) {
                                CompoundTag sectionTag = listTag.getCompoundOrEmpty(i);
                                if (isInvalidSection(sectionTag, minSection, maxSection)) {
                                    continue;
                                }
                                chunkSectionCount++;
                                if (sectionTag.getCompound("block_states").isEmpty()) {
                                    sections[sectionOffset + sectionIndex++] = new LevelChunkSection(
                                        containerFactory.createForBlockStates(),
                                        containerFactory.createForBiomes());
                                    continue;
                                }
                                try {
                                    sections[sectionOffset + sectionIndex++] = new LevelChunkSection(containerFactory.blockStatesContainerCodec()
                                        .parse(NbtOps.INSTANCE, sectionTag.getCompoundOrEmpty("block_states")).promotePartial((sx) -> {
                                            logger.warning("Something went wrong when reading chunk section: " + sx);
                                        }).getOrThrow(), containerFactory.createForBiomes());
                                } catch (IllegalStateException ise) {
                                    logger.warning("Something went wrong when reading chunk section", ise);
                                }
                            }
                            thread.setTask("Creating empty sections");
                            if (chunkSectionCount != sectionCount) {
                                for (int i = chunkSectionCount; i < sectionCount; i++) {
                                    sections[i] = new LevelChunkSection(
                                        containerFactory.createForBlockStates(),
                                        containerFactory.createForBiomes());
                                }
                            }
                            thread.setTask("Loading blocks");
                            int cx = chunkTag.getIntOr("xPos", 0), cz = chunkTag.getIntOr("zPos", 0);
                            net.minecraft.world.level.chunk.ProtoChunk chunk = new net.minecraft.world.level.chunk.ProtoChunk(
                                new ChunkPos(cx, cz), null, sections, null, null, this, containerFactory, null);
                            if (chunkTag.get("ChunkBukkitValues") instanceof CompoundTag persistentDataTag) {
                                chunk.persistentDataContainer.putAll(persistentDataTag);
                            }
                            thread.setTask("Reading entities");
                            Pair<CompoundTag, String> entityTag = readEntityTag(chunkTag, entityRegion, posInRegion);
                            if (entityTag != null) {
                                ListTag entityListTag = entityTag.getFirst().getList(entityTag.getSecond()).filter(list -> list.identifyRawElementType() == 10)
                                    .orElseGet(ListTag::new);
                                for (int i = 0; i < entityListTag.size(); i++) {
                                    chunk.addEntity(entityListTag.getCompoundOrEmpty(i));
                                }
                            }
                            thread.setTask("Loading block entities");
                            Object2IntArrayMap<BlockPos> blockEntityMap = new Object2IntArrayMap<>();
                            blockEntityMap.defaultReturnValue(-1);
                            ListTag blockEntityListTag = chunkTag.getList("block_entities").filter(list -> list.identifyRawElementType() == 10)
                                .orElseGet(ListTag::new);
                            for (int i = 0; i < blockEntityListTag.size(); i++) {
                                CompoundTag blockEntityTag = blockEntityListTag.getCompoundOrEmpty(i);
                                chunk.setBlockEntityNbt(blockEntityTag);
                                blockEntityMap.put(BlockEntity.getPosFromTag(chunk.getPos(), blockEntityTag), i);
                            }
                            thread.setTask("Constructing chunk");
                            ProtoChunk1_21_R6 protoChunk = new ProtoChunk1_21_R6(this, chunk, cx, cz);
                            thread.setTask("Running consumer");
                            consumer.accept(protoChunk);
                            if (protoChunk.isDirty()) {
                                thread.setTask("Preparing entites");
                                blockEntityListTag.clear();
                                for (ProtoBlockEntity rawBlock : protoChunk.getBlockEntities()) {
                                    blockEntityListTag.add(((ProtoBlockEntity1_21_R6) rawBlock).tag());
                                }
                                thread.setTask("Preparing sections");
                                sectionIndex = 0;
                                for (int i = 0; i < listTag.size(); i++) {
                                    CompoundTag sectionTag = listTag.getCompoundOrEmpty(i);
                                    if (isInvalidSection(sectionTag, minSection, maxSection)) {
                                        continue;
                                    }
                                    if (sectionTag.getCompound("block_states").isEmpty()) {
                                        sectionIndex++;
                                        continue;
                                    }
                                    try {
                                        sectionTag.put("block_states",
                                            containerFactory.blockStatesContainerCodec()
                                                .encodeStart(NbtOps.INSTANCE, sections[sectionOffset + sectionIndex++].getStates())
                                                .getOrThrow());
                                    } catch (IllegalStateException ise) {
                                        logger.warning("Something went wrong when writing chunk section", ise);
                                    }
                                }
                                thread.setTask("Saving chunk");
                                try (DataOutputStream output = chunkRegion.getChunkDataOutputStream(posInRegion)) {
                                    NbtIo.write(chunkTag, output);
                                }
                                if (entityTag != null && entityTag.getFirst() != chunkTag) {
                                    thread.setTask("Saving external entities");
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
                thread.setTask("Closing files");
            } finally {
                if (entityRegion != null) {
                    entityRegion.close();
                }
            }
        } catch (Throwable e) {
            // Maybe mark as complete? Unsure tho
            counter.increment(counter.max() - counter.current());
            logger.error("Failed to convert region '{1}' in level '{0}'!", e, worldData.getLevelName(), path.getFileName().toString());
        }
        thread.setRegion(null);
        thread.setChunk(0, 0);
        thread.setTask(null);
    }

    private boolean isInvalidSection(CompoundTag sectionTag, int min, int max) {
        Optional<Byte> optional = sectionTag.getByte("Y");
        if (optional.isEmpty()) {
            return true;
        }
        byte y = optional.get();
        return y < min || y >= max;
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
        if (tag == null || tag.getString("Status").isEmpty()) {
            return null;
        }
        return chunkStorage.upgradeChunkTag(dimensionKey, () -> null, tag, Optional.empty(), pos, null);
    }

    private Pair<CompoundTag, String> readEntityTag(CompoundTag chunkTag, RegionFile file, ChunkPos pos) throws IOException {
        ChunkStatus status = ChunkStatus.byName(chunkTag.getStringOr("Status", ""));
        if (status.getChunkType() == ChunkType.PROTOCHUNK) {
            return Pair.of(chunkTag, "entities");
        }
        if (file == null || !file.doesChunkExist(pos)) {
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

    final Frozen registry() {
        return registry;
    }

    @Override
    public int getHeight() {
        return dimensionType.height();
    }

    @Override
    public int getMinY() {
        return dimensionType.minY();
    }

}
