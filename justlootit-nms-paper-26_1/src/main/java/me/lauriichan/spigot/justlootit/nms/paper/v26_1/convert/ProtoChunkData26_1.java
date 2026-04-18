package me.lauriichan.spigot.justlootit.nms.paper.v26_1.convert;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;

import ca.spottedleaf.moonrise.common.util.MixinWorkarounds;
import ca.spottedleaf.moonrise.common.util.WorldUtil;
import ca.spottedleaf.moonrise.patches.starlight.light.SWMRNibbleArray;
import ca.spottedleaf.moonrise.patches.starlight.light.SWMRNibbleArray.SaveState;
import ca.spottedleaf.moonrise.patches.starlight.light.StarLightEngine;
import ca.spottedleaf.moonrise.patches.starlight.storage.StarlightSectionData;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.Optionull;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.ChunkAccess.PackedTicks;
import net.minecraft.world.level.chunk.LevelChunk.PostLoadProcessor;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.blending.BlendingData.Packed;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import org.slf4j.Logger;

/**
 * Copy of {@link SerializableChunkData} in Spigot
 */
@SuppressWarnings({
    "unused",
    "unchecked",
    "rawtypes",
    "serial"
})
public record ProtoChunkData26_1(PalettedContainerFactory containerFactory, ChunkPos chunkPos, int minSectionY, long lastUpdateTime,
    long inhabitedTime, ChunkStatus chunkStatus, Packed blendingData, BelowZeroRetrogen belowZeroRetrogen, UpgradeData upgradeData,
    long[] carvingMask, Map<Types, long[]> heightmaps, PackedTicks packedTicks, ShortList[] postProcessingSections, boolean lightCorrect,
    List<ProtoChunkData26_1.SectionData> sectionData, List<CompoundTag> entities, List<CompoundTag> blockEntities,
    CompoundTag structureData, Tag persistentDataContainer) {
    private static final Codec<List<SavedTick<Block>>> BLOCK_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.BLOCK.byNameCodec()).listOf();
    private static final Codec<List<SavedTick<Fluid>>> FLUID_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.FLUID.byNameCodec()).listOf();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_UPGRADE_DATA = "UpgradeData";
    private static final String BLOCK_TICKS_TAG = "block_ticks";
    private static final String FLUID_TICKS_TAG = "fluid_ticks";
    public static final String X_POS_TAG = "xPos";
    public static final String Z_POS_TAG = "zPos";
    public static final String HEIGHTMAPS_TAG = "Heightmaps";
    public static final String IS_LIGHT_ON_TAG = "isLightOn";
    public static final String SECTIONS_TAG = "sections";
    public static final String BLOCK_LIGHT_TAG = "BlockLight";
    public static final String SKY_LIGHT_TAG = "SkyLight";
    private static final int CURRENT_DATA_VERSION = SharedConstants.getCurrentVersion().dataVersion().version();
    private static final boolean JUST_CORRUPT_IT = Boolean.getBoolean("Paper.ignoreWorldDataVersion");

    public static ChunkPos getChunkCoordinate(CompoundTag chunkData) {
        int dataVersion = NbtUtils.getDataVersion(chunkData);
        if (dataVersion < 2842) {
            CompoundTag levelData = chunkData.getCompoundOrEmpty("Level");
            return new ChunkPos(levelData.getIntOr("xPos", 0), levelData.getIntOr("zPos", 0));
        } else {
            return new ChunkPos(chunkData.getIntOr("xPos", 0), chunkData.getIntOr("zPos", 0));
        }
    }

    public static long getLastWorldSaveTime(CompoundTag chunkData) {
        int dataVersion = NbtUtils.getDataVersion(chunkData);
        if (dataVersion < 2842) {
            CompoundTag levelData = chunkData.getCompoundOrEmpty("Level");
            return levelData.getLongOr("LastUpdate", 0L);
        } else {
            return chunkData.getLongOr("LastUpdate", 0L);
        }
    }

    public static ProtoChunkData26_1 parse(LevelHeightAccessor levelHeight, PalettedContainerFactory containerFactory,
        CompoundTag chunkData) {
        ServerLevel serverLevel = (ServerLevel) levelHeight;
        if (chunkData.getString("Status").isEmpty()) {
            return null;
        } else {
            chunkData.getInt("DataVersion").ifPresent(dataVersion -> {
                if (!JUST_CORRUPT_IT && dataVersion > CURRENT_DATA_VERSION) {
                    new RuntimeException("Server attempted to load chunk saved with newer version of minecraft! " + dataVersion + " > "
                        + CURRENT_DATA_VERSION).printStackTrace();
                    System.exit(1);
                }
            });
            ChunkPos chunkPos = new ChunkPos(chunkData.getIntOr("xPos", 0), chunkData.getIntOr("zPos", 0));
            long lastUpdateTime = chunkData.getLongOr("LastUpdate", 0L);
            long inhabitedTime = chunkData.getLongOr("InhabitedTime", 0L);
            ChunkStatus status = (ChunkStatus) chunkData.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY);
            UpgradeData upgradeData = (UpgradeData) chunkData.getCompound("UpgradeData").map(tag -> new UpgradeData(tag, levelHeight))
                .orElse(UpgradeData.EMPTY);
            boolean lightCorrect = status.isOrAfter(ChunkStatus.LIGHT) && chunkData.get("isLightOn") != null
                && chunkData.getIntOr("starlight.light_version", -1) == 10;
            Packed blendingData = (Packed) chunkData.read("blending_data", Packed.CODEC).orElse(null);
            BelowZeroRetrogen belowZeroRetrogen = (BelowZeroRetrogen) chunkData.read("below_zero_retrogen", BelowZeroRetrogen.CODEC)
                .orElse(null);
            long[] carvingMask = (long[]) chunkData.getLongArray("carving_mask").orElse(null);
            Map<Types, long[]> heightmaps = new EnumMap(Types.class);
            chunkData.getCompound("Heightmaps").ifPresent(heightmapsTag -> {
                for (Types type : status.heightmapsAfter()) {
                    heightmapsTag.getLongArray(type.getSerializationKey()).ifPresent(longs -> heightmaps.put(type, longs));
                }
            });
            List<SavedTick<Block>> blockTicks = SavedTick
                .filterTickListForChunk((List) chunkData.read("block_ticks", BLOCK_TICKS_CODEC).orElse(List.of()), chunkPos);
            List<SavedTick<Fluid>> fluidTicks = SavedTick
                .filterTickListForChunk((List) chunkData.read("fluid_ticks", FLUID_TICKS_CODEC).orElse(List.of()), chunkPos);
            PackedTicks packedTicks = new PackedTicks(blockTicks, fluidTicks);
            ListTag postProcessTags = chunkData.getListOrEmpty("PostProcessing");
            ShortList[] postProcessingSections = new ShortList[postProcessTags.size()];

            for (int sectionIndex = 0; sectionIndex < postProcessTags.size(); ++sectionIndex) {
                ListTag offsetsTag = (ListTag) postProcessTags.getList(sectionIndex).orElse(null);
                if (offsetsTag != null && !offsetsTag.isEmpty()) {
                    ShortList packedOffsets = new ShortArrayList(offsetsTag.size());

                    for (int i = 0; i < offsetsTag.size(); ++i) {
                        packedOffsets.add(offsetsTag.getShortOr(i, (short) 0));
                    }

                    postProcessingSections[sectionIndex] = packedOffsets;
                }
            }

            List<CompoundTag> entities = chunkData.getList("entities").stream().flatMap(ListTag::compoundStream).toList();
            List<CompoundTag> blockEntities = chunkData.getList("block_entities").stream().flatMap(ListTag::compoundStream).toList();
            CompoundTag structureData = chunkData.getCompoundOrEmpty("structures");
            ListTag sectionTags = chunkData.getListOrEmpty("sections");
            List<ProtoChunkData26_1.SectionData> sectionData = new ArrayList(sectionTags.size());
            Codec<PalettedContainer<Holder<Biome>>> biomesCodec = containerFactory.biomeContainerRWCodec();
            Codec<PalettedContainer<BlockState>> blockStatesCodec = containerFactory.blockStatesContainerCodec();

            for (int i = 0; i < sectionTags.size(); ++i) {
                Optional<CompoundTag> maybeSectionTag = sectionTags.getCompound(i);
                if (!maybeSectionTag.isEmpty()) {
                    CompoundTag sectionTag = (CompoundTag) maybeSectionTag.get();
                    int y = sectionTag.getByteOr("Y", (byte) 0);
                    LevelChunkSection section;
                    if (y >= levelHeight.getMinSectionY() && y <= levelHeight.getMaxSectionY()) {
                        BlockState[] presetBlockStates = serverLevel.chunkPacketBlockController.getPresetBlockStates(serverLevel, chunkPos,
                            y);
                        Codec<PalettedContainer<BlockState>> antiXrayBlockStateCodec = presetBlockStates == null ? blockStatesCodec
                            : PalettedContainer.codecRW(BlockState.CODEC, containerFactory.blockStatesStrategy(),
                                Blocks.AIR.defaultBlockState(), presetBlockStates);
                        PalettedContainer<BlockState> blocks = (PalettedContainer) sectionTag.getCompound("block_states")
                            .map(container -> (PalettedContainer) antiXrayBlockStateCodec.parse(NbtOps.INSTANCE, container)
                                .promotePartial(msg -> logErrors(chunkPos, y, msg)).getOrThrow(ProtoChunkData26_1.ChunkReadException::new))
                            .orElseGet(containerFactory::createForBlockStates);
                        PalettedContainer<Holder<Biome>> biomes = (PalettedContainer) sectionTag.getCompound("biomes")
                            .map(container -> (PalettedContainer) biomesCodec.parse(NbtOps.INSTANCE, container)
                                .promotePartial(msg -> logErrors(chunkPos, y, msg)).getOrThrow(ProtoChunkData26_1.ChunkReadException::new))
                            .orElseGet(containerFactory::createForBiomes);
                        section = new LevelChunkSection(blocks, biomes);
                    } else {
                        section = null;
                    }

                    DataLayer blockLight = (DataLayer) sectionTag.getByteArray("BlockLight").map(DataLayer::new).orElse(null);
                    DataLayer skyLight = (DataLayer) sectionTag.getByteArray("SkyLight").map(DataLayer::new).orElse(null);
                    ProtoChunkData26_1.SectionData ProtoChunkData26_1 = new ProtoChunkData26_1.SectionData(y, section, blockLight,
                        skyLight);
                    if (sectionTag.contains("starlight.blocklight_state")) {
                        ProtoChunkData26_1.starlight$setBlockLightState(sectionTag.getIntOr("starlight.blocklight_state", 0));
                    }

                    if (sectionTag.contains("starlight.skylight_state")) {
                        ProtoChunkData26_1.starlight$setSkyLightState(sectionTag.getIntOr("starlight.skylight_state", 0));
                    }

                    sectionData.add(ProtoChunkData26_1);
                }
            }

            return new ProtoChunkData26_1(containerFactory, chunkPos, levelHeight.getMinSectionY(), lastUpdateTime, inhabitedTime, status,
                blendingData, belowZeroRetrogen, upgradeData, carvingMask, heightmaps, packedTicks, postProcessingSections, lightCorrect,
                sectionData, entities, blockEntities, structureData, chunkData.get("ChunkBukkitValues"));
        }
    }

    private ProtoChunk loadStarlightLightData(ServerLevel world, ProtoChunk ret) {
        boolean hasSkyLight = world.dimensionType().hasSkyLight();
        int minSection = WorldUtil.getMinLightSection(world);
        SWMRNibbleArray[] blockNibbles = StarLightEngine.getFilledEmptyLight(world);
        SWMRNibbleArray[] skyNibbles = StarLightEngine.getFilledEmptyLight(world);
        if (!this.lightCorrect) {
            ret.starlight$setBlockNibbles(blockNibbles);
            ret.starlight$setSkyNibbles(skyNibbles);
            return ret;
        } else {
            try {
                for (ProtoChunkData26_1.SectionData sectionData : this.sectionData) {
                    int y = sectionData.y();
                    DataLayer blockLight = sectionData.blockLight();
                    DataLayer skyLight = sectionData.skyLight();
                    int blockState = sectionData.starlight$getBlockLightState();
                    int skyState = sectionData.starlight$getSkyLightState();
                    if (blockState >= 0) {
                        if (blockLight != null) {
                            blockNibbles[y - minSection] = new SWMRNibbleArray(MixinWorkarounds.clone(blockLight.getData()), blockState);
                        } else {
                            blockNibbles[y - minSection] = new SWMRNibbleArray(null, blockState);
                        }
                    }

                    if (skyState >= 0 && hasSkyLight) {
                        if (skyLight != null) {
                            skyNibbles[y - minSection] = new SWMRNibbleArray(MixinWorkarounds.clone(skyLight.getData()), skyState);
                        } else {
                            skyNibbles[y - minSection] = new SWMRNibbleArray(null, skyState);
                        }
                    }
                }

                ret.starlight$setBlockNibbles(blockNibbles);
                ret.starlight$setSkyNibbles(skyNibbles);
            } catch (Throwable var14) {
                ret.setLightCorrect(false);
                LOGGER.error("Failed to parse light data for chunk " + ret.getPos() + " in world '" + WorldUtil.getWorldName(world) + "'",
                    var14);
            }

            return ret;
        }
    }

    public ProtoChunk read(ServerLevel level, PoiManager poiManager, RegionStorageInfo regionInfo, ChunkPos pos) {
        if (!Objects.equals(pos, this.chunkPos)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", new Object[] {
                pos,
                pos,
                this.chunkPos
            });
            level.getServer().reportMisplacedChunk(this.chunkPos, pos, regionInfo);
        }

        int sectionCount = level.getSectionsCount();
        LevelChunkSection[] sections = new LevelChunkSection[sectionCount];
        boolean skyLight = level.dimensionType().hasSkyLight();
        ChunkSource chunkSource = level.getChunkSource();
        LevelLightEngine lightEngine = chunkSource.getLightEngine();
        PalettedContainerFactory containerFactory = level.palettedContainerFactory();
        boolean loadedAnyLight = false;

        for (ProtoChunkData26_1.SectionData section : this.sectionData) {
            SectionPos sectionPos = SectionPos.of(pos, section.y);
            if (section.chunkSection != null) {
                sections[level.getSectionIndexFromSectionY(section.y)] = section.chunkSection;
            }

            boolean hasBlockLight = section.blockLight != null;
            boolean hasSkyLight = skyLight && section.skyLight != null;
            if (hasBlockLight || hasSkyLight) {
                if (!loadedAnyLight) {
                    lightEngine.retainData(pos, true);
                    loadedAnyLight = true;
                }

                if (hasBlockLight) {
                    lightEngine.queueSectionData(LightLayer.BLOCK, sectionPos, section.blockLight);
                }

                if (hasSkyLight) {
                    lightEngine.queueSectionData(LightLayer.SKY, sectionPos, section.skyLight);
                }
            }
        }

        ChunkType chunkType = this.chunkStatus.getChunkType();
        ChunkAccess chunk;
        if (chunkType == ChunkType.LEVELCHUNK) {
            LevelChunkTicks<Block> blockTicks = new LevelChunkTicks(this.packedTicks.blocks());
            LevelChunkTicks<Fluid> fluidTicks = new LevelChunkTicks(this.packedTicks.fluids());
            chunk = new LevelChunk(level.getLevel(), pos, this.upgradeData, blockTicks, fluidTicks, this.inhabitedTime, sections,
                postLoadChunk(level, this.entities, this.blockEntities), BlendingData.unpack(this.blendingData));
        } else {
            ProtoChunkTicks<Block> blockTicks = ProtoChunkTicks.load(this.packedTicks.blocks());
            ProtoChunkTicks<Fluid> fluidTicks = ProtoChunkTicks.load(this.packedTicks.fluids());
            ProtoChunk protoChunk = new ProtoChunk(pos, this.upgradeData, sections, blockTicks, fluidTicks, level, containerFactory,
                BlendingData.unpack(this.blendingData));
            chunk = protoChunk;
            protoChunk.setInhabitedTime(this.inhabitedTime);
            if (this.belowZeroRetrogen != null) {
                protoChunk.setBelowZeroRetrogen(this.belowZeroRetrogen);
            }

            protoChunk.setPersistedStatus(this.chunkStatus);
            if (this.chunkStatus.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
                protoChunk.setLightEngine(lightEngine);
            }
        }

        Tag var26 = this.persistentDataContainer;
        if (var26 instanceof CompoundTag compoundTag) {
            chunk.persistentDataContainer.putAll(compoundTag);
        }

        chunk.setLightCorrect(this.lightCorrect);
        EnumSet<Types> toPrime = EnumSet.noneOf(Types.class);

        for (Types type : chunk.getPersistedStatus().heightmapsAfter()) {
            long[] heightmap = (long[]) this.heightmaps.get(type);
            if (heightmap != null) {
                chunk.setHeightmap(type, heightmap);
            } else {
                toPrime.add(type);
            }
        }

        Heightmap.primeHeightmaps(chunk, toPrime);
        chunk.setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(level), this.structureData, level.getSeed()));
        chunk.setAllReferences(unpackStructureReferences(level.registryAccess(), pos, this.structureData));

        for (int sectionIndex = 0; sectionIndex < this.postProcessingSections.length; ++sectionIndex) {
            ShortList postProcessingSection = this.postProcessingSections[sectionIndex];
            if (postProcessingSection != null) {
                chunk.addPackedPostProcess(postProcessingSection, sectionIndex);
            }
        }

        if (chunkType == ChunkType.LEVELCHUNK) {
            return this.loadStarlightLightData(level, new ImposterProtoChunk((LevelChunk) chunk, false));
        } else {
            ProtoChunk protoChunkx = (ProtoChunk) chunk;

            for (CompoundTag entity : this.entities) {
                protoChunkx.addEntity(entity);
            }

            for (CompoundTag blockEntity : this.blockEntities) {
                protoChunkx.setBlockEntityNbt(blockEntity);
            }

            if (this.carvingMask != null) {
                protoChunkx.setCarvingMask(new CarvingMask(this.carvingMask, chunk.getMinY()));
            }

            return this.loadStarlightLightData(level, protoChunkx);
        }
    }

    private static void logErrors(ChunkPos pos, int sectionY, String message) {
        LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", new Object[] {
            pos.x(),
            sectionY,
            pos.z(),
            message
        });
    }

    public static ProtoChunkData26_1 copyOf(ServerLevel level, ChunkAccess chunk) {
        if (!chunk.canBeSerialized()) {
            throw new IllegalArgumentException("Chunk can't be serialized: " + chunk);
        } else {
            ChunkPos pos = chunk.getPos();
            List<ProtoChunkData26_1.SectionData> sectionData = new ArrayList();
            int minLightSection = WorldUtil.getMinLightSection(level);
            int maxLightSection = WorldUtil.getMaxLightSection(level);
            int minBlockSection = WorldUtil.getMinSection(level);
            LevelChunkSection[] chunkSections = chunk.getSections();
            SWMRNibbleArray[] blockNibbles = chunk.starlight$getBlockNibbles();
            SWMRNibbleArray[] skyNibbles = chunk.starlight$getSkyNibbles();

            for (int lightSection = minLightSection; lightSection <= maxLightSection; ++lightSection) {
                int lightSectionIdx = lightSection - minLightSection;
                int blockSectionIdx = lightSection - minBlockSection;
                LevelChunkSection chunkSection = blockSectionIdx >= 0 && blockSectionIdx < chunkSections.length
                    ? chunkSections[blockSectionIdx].copy()
                    : null;
                SaveState blockNibble = blockNibbles[lightSectionIdx].getSaveState();
                SaveState skyNibble = skyNibbles[lightSectionIdx].getSaveState();
                if (chunkSection != null || blockNibble != null || skyNibble != null) {
                    ProtoChunkData26_1.SectionData section = new ProtoChunkData26_1.SectionData(lightSection, chunkSection,
                        blockNibble == null ? null : (blockNibble.data == null ? null : new DataLayer(blockNibble.data)),
                        skyNibble == null ? null : (skyNibble.data == null ? null : new DataLayer(skyNibble.data)));
                    if (blockNibble != null) {
                        section.starlight$setBlockLightState(blockNibble.state);
                    }

                    if (skyNibble != null) {
                        section.starlight$setSkyLightState(skyNibble.state);
                    }

                    sectionData.add(section);
                }
            }

            List<CompoundTag> blockEntities = new ArrayList(chunk.getBlockEntitiesPos().size());

            for (BlockPos blockPos : chunk.getBlockEntitiesPos()) {
                CompoundTag blockEntityTag = chunk.getBlockEntityNbtForSaving(blockPos, level.registryAccess());
                if (blockEntityTag != null) {
                    blockEntities.add(blockEntityTag);
                }
            }

            List<CompoundTag> entities = new ArrayList();
            long[] carvingMask = null;
            if (chunk.getPersistedStatus().getChunkType() == ChunkType.PROTOCHUNK) {
                ProtoChunk protoChunk = (ProtoChunk) chunk;
                entities.addAll(protoChunk.getEntities());
                CarvingMask existingMask = protoChunk.getCarvingMask();
                if (existingMask != null) {
                    carvingMask = existingMask.toArray();
                }
            }

            Map<Types, long[]> heightmaps = new EnumMap(Types.class);

            for (Entry<Types, Heightmap> entry : chunk.getHeightmaps()) {
                if (chunk.getPersistedStatus().heightmapsAfter().contains(entry.getKey())) {
                    long[] data = ((Heightmap) entry.getValue()).getRawData();
                    heightmaps.put((Types) entry.getKey(), (long[]) data.clone());
                }
            }

            PackedTicks ticksForSerialization = chunk.getTicksForSerialization(level.getGameTime());
            ShortList[] postProcessingSections = (ShortList[]) Arrays.stream(chunk.getPostProcessing())
                .map(shorts -> shorts != null && !shorts.isEmpty() ? new ShortArrayList(shorts) : null).toArray(x$0 -> new ShortList[x$0]);
            CompoundTag structureData = packStructureData(StructurePieceSerializationContext.fromLevel(level), pos, chunk.getAllStarts(),
                chunk.getAllReferences());
            CompoundTag persistentDataContainer = null;
            if (!chunk.persistentDataContainer.isEmpty()) {
                persistentDataContainer = chunk.persistentDataContainer.toTagCompound();
            }

            return new ProtoChunkData26_1(level.palettedContainerFactory(), pos, chunk.getMinSectionY(), level.getGameTime(),
                chunk.getInhabitedTime(), chunk.getPersistedStatus(), (Packed) Optionull.map(chunk.getBlendingData(), BlendingData::pack),
                chunk.getBelowZeroRetrogen(), chunk.getUpgradeData().copy(), carvingMask, heightmaps, ticksForSerialization,
                postProcessingSections, chunk.isLightCorrect(), sectionData, entities, blockEntities, structureData,
                persistentDataContainer);
        }
    }

    public CompoundTag write() {
        CompoundTag tag = NbtUtils.addCurrentDataVersion(new CompoundTag());
        tag.putInt("xPos", this.chunkPos.x());
        tag.putInt("yPos", this.minSectionY);
        tag.putInt("zPos", this.chunkPos.z());
        tag.putLong("LastUpdate", this.lastUpdateTime);
        tag.putLong("InhabitedTime", this.inhabitedTime);
        tag.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(this.chunkStatus).toString());
        tag.storeNullable("blending_data", Packed.CODEC, this.blendingData);
        tag.storeNullable("below_zero_retrogen", BelowZeroRetrogen.CODEC, this.belowZeroRetrogen);
        if (!this.upgradeData.isEmpty()) {
            tag.put("UpgradeData", this.upgradeData.write());
        }

        ListTag sectionTags = new ListTag();
        Codec<PalettedContainer<BlockState>> blockStatesCodec = this.containerFactory.blockStatesContainerCodec();
        Codec<PalettedContainerRO<Holder<Biome>>> biomeCodec = this.containerFactory.biomeContainerCodec();

        for (ProtoChunkData26_1.SectionData section : this.sectionData) {
            CompoundTag sectionTag = new CompoundTag();
            LevelChunkSection chunkSection = section.chunkSection;
            if (chunkSection != null) {
                sectionTag.store("block_states", blockStatesCodec, chunkSection.getStates());
                sectionTag.store("biomes", biomeCodec, chunkSection.getBiomes());
            }

            if (section.blockLight != null) {
                sectionTag.putByteArray("BlockLight", section.blockLight.getData());
            }

            if (section.skyLight != null) {
                sectionTag.putByteArray("SkyLight", section.skyLight.getData());
            }

            int blockState = section.starlight$getBlockLightState();
            int skyState = section.starlight$getSkyLightState();
            if (blockState > 0) {
                sectionTag.putInt("starlight.blocklight_state", blockState);
            }

            if (skyState > 0) {
                sectionTag.putInt("starlight.skylight_state", skyState);
            }

            if (!sectionTag.isEmpty()) {
                sectionTag.putByte("Y", (byte) section.y);
                sectionTags.add(sectionTag);
            }
        }

        tag.put("sections", sectionTags);
        if (this.lightCorrect) {
            tag.putBoolean("isLightOn", true);
        }

        ListTag blockEntityTags = new ListTag();
        blockEntityTags.addAll(this.blockEntities);
        tag.put("block_entities", blockEntityTags);
        if (this.chunkStatus.getChunkType() == ChunkType.PROTOCHUNK) {
            ListTag entityTags = new ListTag();
            entityTags.addAll(this.entities);
            tag.put("entities", entityTags);
            if (this.carvingMask != null) {
                tag.putLongArray("carving_mask", this.carvingMask);
            }
        }

        saveTicks(tag, this.packedTicks);
        tag.put("PostProcessing", packOffsets(this.postProcessingSections));
        CompoundTag heightmapsTag = new CompoundTag();
        this.heightmaps.forEach((type, data) -> heightmapsTag.put(type.getSerializationKey(), new LongArrayTag(data)));
        tag.put("Heightmaps", heightmapsTag);
        tag.put("structures", this.structureData);
        if (this.persistentDataContainer != null) {
            tag.put("ChunkBukkitValues", this.persistentDataContainer);
        }

        if (this.lightCorrect && !this.chunkStatus.isBefore(ChunkStatus.LIGHT)) {
            tag.putBoolean("isLightOn", false);
            tag.putInt("starlight.light_version", 10);
        }

        return tag;
    }

    private static void saveTicks(CompoundTag levelData, PackedTicks ticksForSerialization) {
        levelData.store("block_ticks", BLOCK_TICKS_CODEC, ticksForSerialization.blocks());
        levelData.store("fluid_ticks", FLUID_TICKS_CODEC, ticksForSerialization.fluids());
    }

    public static ChunkStatus getChunkStatusFromTag(CompoundTag tag) {
        return tag != null ? (ChunkStatus) tag.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY) : ChunkStatus.EMPTY;
    }

    private static PostLoadProcessor postLoadChunk(ServerLevel level, List<CompoundTag> entities, List<CompoundTag> blockEntities) {
        return entities.isEmpty() && blockEntities.isEmpty() ? null : levelChunk -> {
            if (!entities.isEmpty()) {
                ScopedCollector reporter = new ScopedCollector(levelChunk.problemPath(), LOGGER);

                try {
                    level.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(
                        TagValueInput.create(reporter, level.registryAccess(), entities), level, EntitySpawnReason.LOAD));
                } catch (Throwable var10) {
                    try {
                        reporter.close();
                    } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                    }

                    throw var10;
                }

                reporter.close();
            }

            for (CompoundTag entityTag : blockEntities) {
                boolean keepPacked = entityTag.getBooleanOr("keepPacked", false);
                if (keepPacked) {
                    levelChunk.setBlockEntityNbt(entityTag);
                } else {
                    BlockPos pos = BlockEntity.getPosFromTag(levelChunk.getPos(), entityTag);
                    BlockEntity blockEntity = BlockEntity.loadStatic(pos, levelChunk.getBlockState(pos), entityTag, level.registryAccess());
                    if (blockEntity != null) {
                        levelChunk.setBlockEntity(blockEntity);
                    }
                }
            }
        };
    }

    private static CompoundTag packStructureData(StructurePieceSerializationContext context, ChunkPos pos,
        Map<Structure, StructureStart> starts, Map<Structure, LongSet> references) {
        CompoundTag outTag = new CompoundTag();
        CompoundTag startsTag = new CompoundTag();
        Registry<Structure> structuresRegistry = context.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        for (Entry<Structure, StructureStart> entry : starts.entrySet()) {
            Identifier key = structuresRegistry.getKey((Structure) entry.getKey());
            startsTag.put(key.toString(), ((StructureStart) entry.getValue()).createTag(context, pos));
        }

        outTag.put("starts", startsTag);
        CompoundTag referencesTag = new CompoundTag();

        for (Entry<Structure, LongSet> entry : references.entrySet()) {
            if (!((LongSet) entry.getValue()).isEmpty()) {
                Identifier key = structuresRegistry.getKey((Structure) entry.getKey());
                referencesTag.putLongArray(key.toString(), ((LongSet) entry.getValue()).toLongArray());
            }
        }

        outTag.put("References", referencesTag);
        return outTag;
    }

    private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext context, CompoundTag tag,
        long seed) {
        Map<Structure, StructureStart> outmap = Maps.newHashMap();
        Registry<Structure> structuresRegistry = context.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        CompoundTag startsTag = tag.getCompoundOrEmpty("starts");

        for (String key : startsTag.keySet()) {
            Identifier id = Identifier.tryParse(key);
            Structure startFeature = (Structure) structuresRegistry.getValue(id);
            if (startFeature == null) {
                LOGGER.error("Unknown structure start: {}", id);
            } else {
                StructureStart start = StructureStart.loadStaticStart(context, startsTag.getCompoundOrEmpty(key), seed);
                if (start != null) {
                    Tag persistentBase = startsTag.getCompoundOrEmpty(key).get("StructureBukkitValues");
                    if (persistentBase instanceof CompoundTag compoundTag) {
                        start.persistentDataContainer.putAll(compoundTag);
                    }

                    outmap.put(startFeature, start);
                }
            }
        }

        return outmap;
    }

    private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess registryAccess, ChunkPos pos, CompoundTag tag) {
        Map<Structure, LongSet> outmap = Maps.newHashMap();
        Registry<Structure> structuresRegistry = registryAccess.lookupOrThrow(Registries.STRUCTURE);
        CompoundTag referencesTag = tag.getCompoundOrEmpty("References");
        referencesTag.forEach((key, entry) -> {
            Identifier structureId = Identifier.tryParse(key);
            Structure structureType = (Structure) structuresRegistry.getValue(structureId);
            if (structureType == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", structureId, pos);
            } else {
                Optional<long[]> longArray = entry.asLongArray();
                if (!longArray.isEmpty()) {
                    outmap.put(structureType, new LongOpenHashSet(Arrays.stream((long[]) longArray.get()).filter(chunkLongPos -> {
                        ChunkPos refPos = ChunkPos.unpack(chunkLongPos);
                        if (refPos.getChessboardDistance(pos) > 8) {
                            LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", new Object[] {
                                structureId,
                                refPos,
                                pos
                            });
                            return false;
                        } else {
                            return true;
                        }
                    }).toArray()));
                }
            }
        });
        return outmap;
    }

    private static ListTag packOffsets(ShortList[] sections) {
        ListTag listTag = new ListTag();

        for (ShortList offsetList : sections) {
            ListTag offsetsTag = new ListTag();
            if (offsetList != null) {
                for (int i = 0; i < offsetList.size(); ++i) {
                    offsetsTag.add(ShortTag.valueOf(offsetList.getShort(i)));
                }
            }

            listTag.add(offsetsTag);
        }

        return listTag;
    }

    public static class ChunkReadException extends NbtException {
        public ChunkReadException(String message) {
            super(message);
        }
    }

    public static final class SectionData implements StarlightSectionData {
        private final int y;

        private final LevelChunkSection chunkSection;

        private final DataLayer blockLight;

        private final DataLayer skyLight;
        private int blockLightState = -1;
        private int skyLightState = -1;

        public final int starlight$getBlockLightState() {
            return this.blockLightState;
        }

        public final void starlight$setBlockLightState(int state) {
            this.blockLightState = state;
        }

        public final int starlight$getSkyLightState() {
            return this.skyLightState;
        }

        public final void starlight$setSkyLightState(int state) {
            this.skyLightState = state;
        }

        public SectionData(int y, LevelChunkSection chunkSection, DataLayer blockLight, DataLayer skyLight) {
            this.y = y;
            this.chunkSection = chunkSection;
            this.blockLight = blockLight;
            this.skyLight = skyLight;
        }

        public int y() {
            return this.y;
        }

        public LevelChunkSection chunkSection() {
            return this.chunkSection;
        }

        public DataLayer blockLight() {
            return this.blockLight;
        }

        public DataLayer skyLight() {
            return this.skyLight;
        }
    }
}
