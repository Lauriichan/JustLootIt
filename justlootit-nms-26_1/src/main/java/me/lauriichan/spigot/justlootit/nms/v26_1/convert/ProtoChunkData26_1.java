package me.lauriichan.spigot.justlootit.nms.v26_1.convert;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
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
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
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
import org.jspecify.annotations.Nullable;
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
    long inhabitedTime, ChunkStatus chunkStatus, @Nullable Packed blendingData, @Nullable BelowZeroRetrogen belowZeroRetrogen,
    UpgradeData upgradeData, @Nullable long[] carvingMask, Map<Types, long[]> heightmaps, PackedTicks packedTicks,
    ShortList[] postProcessingSections, boolean lightCorrect, List<ProtoChunkData26_1.SectionData> sectionData, List<CompoundTag> entities,
    List<CompoundTag> blockEntities, CompoundTag structureData, @Nullable Tag persistentDataContainer) {
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

    public static ProtoChunkData26_1 parse(LevelHeightAccessor levelHeight, PalettedContainerFactory containerFactory,
        CompoundTag chunkData) {
        if (chunkData.getString("Status").isEmpty()) {
            return null;
        } else {
            ChunkPos chunkpos = new ChunkPos(chunkData.getIntOr("xPos", 0), chunkData.getIntOr("zPos", 0));
            long i = chunkData.getLongOr("LastUpdate", 0L);
            long j = chunkData.getLongOr("InhabitedTime", 0L);
            ChunkStatus chunkstatus = (ChunkStatus) chunkData.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY);
            UpgradeData upgradedata = (UpgradeData) chunkData.getCompound("UpgradeData")
                .map(compoundtag1x -> new UpgradeData(compoundtag1x, levelHeight)).orElse(UpgradeData.EMPTY);
            boolean flag = chunkData.getBooleanOr("isLightOn", false);
            Packed blendingdata_packed = (Packed) chunkData.read("blending_data", Packed.CODEC).orElse(null);
            BelowZeroRetrogen belowzeroretrogen = (BelowZeroRetrogen) chunkData.read("below_zero_retrogen", BelowZeroRetrogen.CODEC)
                .orElse(null);
            long[] along = (long[]) chunkData.getLongArray("carving_mask").orElse(null);
            Map<Types, long[]> map = new EnumMap(Types.class);
            chunkData.getCompound("Heightmaps").ifPresent(compoundtag1x -> {
                for (Types heightmap_types : chunkstatus.heightmapsAfter()) {
                    compoundtag1x.getLongArray(heightmap_types.getSerializationKey()).ifPresent(along1 -> map.put(heightmap_types, along1));
                }
            });
            List<SavedTick<Block>> list = SavedTick
                .filterTickListForChunk((List) chunkData.read("block_ticks", BLOCK_TICKS_CODEC).orElse(List.of()), chunkpos);
            List<SavedTick<Fluid>> list1 = SavedTick
                .filterTickListForChunk((List) chunkData.read("fluid_ticks", FLUID_TICKS_CODEC).orElse(List.of()), chunkpos);
            PackedTicks chunkaccess_packedticks = new PackedTicks(list, list1);
            ListTag listtag = chunkData.getListOrEmpty("PostProcessing");
            ShortList[] ashortlist = new ShortList[listtag.size()];

            for (int k = 0; k < listtag.size(); ++k) {
                ListTag listtag1 = (ListTag) listtag.getList(k).orElse(null);
                if (listtag1 != null && !listtag1.isEmpty()) {
                    ShortList shortlist = new ShortArrayList(listtag1.size());

                    for (int l = 0; l < listtag1.size(); ++l) {
                        shortlist.add(listtag1.getShortOr(l, (short) 0));
                    }

                    ashortlist[k] = shortlist;
                }
            }

            List<CompoundTag> list2 = chunkData.getList("entities").stream().flatMap(ListTag::compoundStream).toList();
            List<CompoundTag> list3 = chunkData.getList("block_entities").stream().flatMap(ListTag::compoundStream).toList();
            CompoundTag compoundtag1 = chunkData.getCompoundOrEmpty("structures");
            ListTag listtag2 = chunkData.getListOrEmpty("sections");
            List<ProtoChunkData26_1.SectionData> list4 = new ArrayList(listtag2.size());
            Codec<PalettedContainer<Holder<Biome>>> codec = containerFactory.biomeContainerCodecRW();
            Codec<PalettedContainer<BlockState>> codec1 = containerFactory.blockStatesContainerCodec();

            for (int i1 = 0; i1 < listtag2.size(); ++i1) {
                Optional<CompoundTag> optional = listtag2.getCompound(i1);
                if (!optional.isEmpty()) {
                    CompoundTag compoundtag2 = (CompoundTag) optional.get();
                    int j1 = compoundtag2.getByteOr("Y", (byte) 0);
                    LevelChunkSection levelchunksection;
                    if (j1 >= levelHeight.getMinSectionY() && j1 <= levelHeight.getMaxSectionY()) {
                        Optional optional1 = compoundtag2.getCompound("block_states")
                            .map(compoundtag3 -> (PalettedContainer) codec1.parse(NbtOps.INSTANCE, compoundtag3)
                                .promotePartial(s -> logErrors(chunkpos, j1, s)).getOrThrow(ProtoChunkData26_1.ChunkReadException::new));
                        PalettedContainer<BlockState> palettedcontainer = (PalettedContainer) optional1
                            .orElseGet(containerFactory::createForBlockStates);
                        optional1 = compoundtag2.getCompound("biomes")
                            .map(compoundtag3 -> (PalettedContainerRO) codec.parse(NbtOps.INSTANCE, compoundtag3)
                                .promotePartial(s -> logErrors(chunkpos, j1, s)).getOrThrow(ProtoChunkData26_1.ChunkReadException::new));
                        PalettedContainer<Holder<Biome>> palettedcontainerro = (PalettedContainer) optional1
                            .orElseGet(containerFactory::createForBiomes);
                        levelchunksection = new LevelChunkSection(palettedcontainer, palettedcontainerro);
                    } else {
                        levelchunksection = null;
                    }

                    DataLayer datalayer = (DataLayer) compoundtag2.getByteArray("BlockLight").map(DataLayer::new).orElse(null);
                    DataLayer datalayer1 = (DataLayer) compoundtag2.getByteArray("SkyLight").map(DataLayer::new).orElse(null);
                    list4.add(new ProtoChunkData26_1.SectionData(j1, levelchunksection, datalayer, datalayer1));
                }
            }

            return new ProtoChunkData26_1(containerFactory, chunkpos, levelHeight.getMinSectionY(), i, j, chunkstatus, blendingdata_packed,
                belowzeroretrogen, upgradedata, along, map, chunkaccess_packedticks, ashortlist, flag, list4, list2, list3, compoundtag1,
                chunkData.get("ChunkBukkitValues"));
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

        int i = level.getSectionsCount();
        LevelChunkSection[] alevelchunksection = new LevelChunkSection[i];
        boolean flag = level.dimensionType().hasSkyLight();
        ChunkSource chunksource = level.getChunkSource();
        LevelLightEngine levellightengine = chunksource.getLightEngine();
        PalettedContainerFactory palettedcontainerfactory = level.palettedContainerFactory();
        boolean flag1 = false;

        for (ProtoChunkData26_1.SectionData ProtoChunkData26_1_sectiondata : this.sectionData) {
            SectionPos sectionpos = SectionPos.of(pos, ProtoChunkData26_1_sectiondata.y);
            if (ProtoChunkData26_1_sectiondata.chunkSection != null) {
                alevelchunksection[level
                    .getSectionIndexFromSectionY(ProtoChunkData26_1_sectiondata.y)] = ProtoChunkData26_1_sectiondata.chunkSection;
                poiManager.checkConsistencyWithBlocks(sectionpos, ProtoChunkData26_1_sectiondata.chunkSection);
            }

            boolean flag2 = ProtoChunkData26_1_sectiondata.blockLight != null;
            boolean flag3 = flag && ProtoChunkData26_1_sectiondata.skyLight != null;
            if (flag2 || flag3) {
                if (!flag1) {
                    levellightengine.retainData(pos, true);
                    flag1 = true;
                }

                if (flag2) {
                    levellightengine.queueSectionData(LightLayer.BLOCK, sectionpos, ProtoChunkData26_1_sectiondata.blockLight);
                }

                if (flag3) {
                    levellightengine.queueSectionData(LightLayer.SKY, sectionpos, ProtoChunkData26_1_sectiondata.skyLight);
                }
            }
        }

        ChunkType chunktype = this.chunkStatus.getChunkType();
        ChunkAccess chunkaccess;
        if (chunktype == ChunkType.LEVELCHUNK) {
            LevelChunkTicks<Block> levelchunkticks = new LevelChunkTicks(this.packedTicks.blocks());
            LevelChunkTicks<Fluid> levelchunkticks1 = new LevelChunkTicks(this.packedTicks.fluids());
            chunkaccess = new LevelChunk(level.getLevel(), pos, this.upgradeData, levelchunkticks, levelchunkticks1, this.inhabitedTime,
                alevelchunksection, postLoadChunk(level, this.entities, this.blockEntities), BlendingData.unpack(this.blendingData));
        } else {
            ProtoChunkTicks<Block> protochunkticks = ProtoChunkTicks.load(this.packedTicks.blocks());
            ProtoChunkTicks<Fluid> protochunkticks1 = ProtoChunkTicks.load(this.packedTicks.fluids());
            ProtoChunk protochunk = new ProtoChunk(pos, this.upgradeData, alevelchunksection, protochunkticks, protochunkticks1, level,
                palettedcontainerfactory, BlendingData.unpack(this.blendingData));
            chunkaccess = protochunk;
            protochunk.setInhabitedTime(this.inhabitedTime);
            if (this.belowZeroRetrogen != null) {
                protochunk.setBelowZeroRetrogen(this.belowZeroRetrogen);
            }

            protochunk.setPersistedStatus(this.chunkStatus);
            if (this.chunkStatus.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
                protochunk.setLightEngine(levellightengine);
            }
        }

        if (this.persistentDataContainer instanceof CompoundTag) {
            chunkaccess.persistentDataContainer.putAll((CompoundTag) this.persistentDataContainer);
        }

        chunkaccess.setLightCorrect(this.lightCorrect);
        EnumSet<Types> enumset = EnumSet.noneOf(Types.class);

        for (Types heightmap_types : chunkaccess.getPersistedStatus().heightmapsAfter()) {
            long[] along = (long[]) this.heightmaps.get(heightmap_types);
            if (along != null) {
                chunkaccess.setHeightmap(heightmap_types, along);
            } else {
                enumset.add(heightmap_types);
            }
        }

        Heightmap.primeHeightmaps(chunkaccess, enumset);
        chunkaccess
            .setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(level), this.structureData, level.getSeed()));
        chunkaccess.setAllReferences(unpackStructureReferences(level.registryAccess(), pos, this.structureData));

        for (int j = 0; j < this.postProcessingSections.length; ++j) {
            ShortList shortlist = this.postProcessingSections[j];
            if (shortlist != null) {
                chunkaccess.addPackedPostProcess(shortlist, j);
            }
        }

        if (chunktype == ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk) chunkaccess, false);
        } else {
            ProtoChunk protochunk1 = (ProtoChunk) chunkaccess;

            for (CompoundTag compoundtag : this.entities) {
                protochunk1.addEntity(compoundtag);
            }

            for (CompoundTag compoundtag1 : this.blockEntities) {
                protochunk1.setBlockEntityNbt(compoundtag1);
            }

            if (this.carvingMask != null) {
                protochunk1.setCarvingMask(new CarvingMask(this.carvingMask, chunkaccess.getMinY()));
            }

            return protochunk1;
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
            ChunkPos chunkpos = chunk.getPos();
            List<ProtoChunkData26_1.SectionData> list = new ArrayList();
            LevelChunkSection[] alevelchunksection = chunk.getSections();
            LevelLightEngine levellightengine = level.getChunkSource().getLightEngine();

            for (int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); ++i) {
                int j = chunk.getSectionIndexFromSectionY(i);
                boolean flag = j >= 0 && j < alevelchunksection.length;
                DataLayer datalayer = levellightengine.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkpos, i));
                DataLayer datalayer1 = levellightengine.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkpos, i));
                DataLayer datalayer2 = datalayer != null && !datalayer.isEmpty() ? datalayer.copy() : null;
                DataLayer datalayer3 = datalayer1 != null && !datalayer1.isEmpty() ? datalayer1.copy() : null;
                if (flag || datalayer2 != null || datalayer3 != null) {
                    LevelChunkSection levelchunksection = flag ? alevelchunksection[j].copy() : null;
                    list.add(new ProtoChunkData26_1.SectionData(i, levelchunksection, datalayer2, datalayer3));
                }
            }

            List<CompoundTag> list1 = new ArrayList(chunk.getBlockEntitiesPos().size());

            for (BlockPos blockpos : chunk.getBlockEntitiesPos()) {
                CompoundTag compoundtag = chunk.getBlockEntityNbtForSaving(blockpos, level.registryAccess());
                if (compoundtag != null) {
                    list1.add(compoundtag);
                }
            }

            List<CompoundTag> list2 = new ArrayList();
            long[] along = null;
            if (chunk.getPersistedStatus().getChunkType() == ChunkType.PROTOCHUNK) {
                ProtoChunk protochunk = (ProtoChunk) chunk;
                list2.addAll(protochunk.getEntities());
                CarvingMask carvingmask = protochunk.getCarvingMask();
                if (carvingmask != null) {
                    along = carvingmask.toArray();
                }
            }

            Map<Types, long[]> map = new EnumMap(Types.class);

            for (Entry<Types, Heightmap> map_entry : chunk.getHeightmaps()) {
                if (chunk.getPersistedStatus().heightmapsAfter().contains(map_entry.getKey())) {
                    long[] along1 = ((Heightmap) map_entry.getValue()).getRawData();
                    map.put((Types) map_entry.getKey(), (long[]) along1.clone());
                }
            }

            PackedTicks chunkaccess_packedticks = chunk.getTicksForSerialization(level.getGameTime());
            ShortList[] ashortlist = (ShortList[]) Arrays.stream(chunk.getPostProcessing())
                .map(shortlist -> shortlist != null && !shortlist.isEmpty() ? new ShortArrayList(shortlist) : null)
                .toArray(k -> new ShortList[k]);
            CompoundTag compoundtag1 = packStructureData(StructurePieceSerializationContext.fromLevel(level), chunkpos,
                chunk.getAllStarts(), chunk.getAllReferences());
            CompoundTag persistentDataContainer = null;
            if (!chunk.persistentDataContainer.isEmpty()) {
                persistentDataContainer = chunk.persistentDataContainer.toTagCompound();
            }

            return new ProtoChunkData26_1(level.palettedContainerFactory(), chunkpos, chunk.getMinSectionY(), level.getGameTime(),
                chunk.getInhabitedTime(), chunk.getPersistedStatus(), (Packed) Optionull.map(chunk.getBlendingData(), BlendingData::pack),
                chunk.getBelowZeroRetrogen(), chunk.getUpgradeData().copy(), along, map, chunkaccess_packedticks, ashortlist,
                chunk.isLightCorrect(), list, list2, list1, compoundtag1, persistentDataContainer);
        }
    }

    public CompoundTag write() {
        CompoundTag compoundtag = NbtUtils.addCurrentDataVersion(new CompoundTag());
        compoundtag.putInt("xPos", this.chunkPos.x());
        compoundtag.putInt("yPos", this.minSectionY);
        compoundtag.putInt("zPos", this.chunkPos.z());
        compoundtag.putLong("LastUpdate", this.lastUpdateTime);
        compoundtag.putLong("InhabitedTime", this.inhabitedTime);
        compoundtag.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(this.chunkStatus).toString());
        compoundtag.storeNullable("blending_data", Packed.CODEC, this.blendingData);
        compoundtag.storeNullable("below_zero_retrogen", BelowZeroRetrogen.CODEC, this.belowZeroRetrogen);
        if (!this.upgradeData.isEmpty()) {
            compoundtag.put("UpgradeData", this.upgradeData.write());
        }

        ListTag listtag = new ListTag();
        Codec<PalettedContainer<BlockState>> codec = this.containerFactory.blockStatesContainerCodec();
        Codec<PalettedContainerRO<Holder<Biome>>> codec1 = this.containerFactory.biomeContainerCodec();

        for (ProtoChunkData26_1.SectionData ProtoChunkData26_1_sectiondata : this.sectionData) {
            CompoundTag compoundtag1 = new CompoundTag();
            LevelChunkSection levelchunksection = ProtoChunkData26_1_sectiondata.chunkSection;
            if (levelchunksection != null) {
                compoundtag1.store("block_states", codec, levelchunksection.getStates());
                compoundtag1.store("biomes", codec1, levelchunksection.getBiomes());
            }

            if (ProtoChunkData26_1_sectiondata.blockLight != null) {
                compoundtag1.putByteArray("BlockLight", ProtoChunkData26_1_sectiondata.blockLight.getData());
            }

            if (ProtoChunkData26_1_sectiondata.skyLight != null) {
                compoundtag1.putByteArray("SkyLight", ProtoChunkData26_1_sectiondata.skyLight.getData());
            }

            if (!compoundtag1.isEmpty()) {
                compoundtag1.putByte("Y", (byte) ProtoChunkData26_1_sectiondata.y);
                listtag.add(compoundtag1);
            }
        }

        compoundtag.put("sections", listtag);
        if (this.lightCorrect) {
            compoundtag.putBoolean("isLightOn", true);
        }

        ListTag listtag1 = new ListTag();
        listtag1.addAll(this.blockEntities);
        compoundtag.put("block_entities", listtag1);
        if (this.chunkStatus.getChunkType() == ChunkType.PROTOCHUNK) {
            ListTag listtag2 = new ListTag();
            listtag2.addAll(this.entities);
            compoundtag.put("entities", listtag2);
            if (this.carvingMask != null) {
                compoundtag.putLongArray("carving_mask", this.carvingMask);
            }
        }

        saveTicks(compoundtag, this.packedTicks);
        compoundtag.put("PostProcessing", packOffsets(this.postProcessingSections));
        CompoundTag compoundtag2 = new CompoundTag();
        this.heightmaps
            .forEach((heightmap_types, along) -> compoundtag2.put(heightmap_types.getSerializationKey(), new LongArrayTag(along)));
        compoundtag.put("Heightmaps", compoundtag2);
        compoundtag.put("structures", this.structureData);
        if (this.persistentDataContainer != null) {
            compoundtag.put("ChunkBukkitValues", this.persistentDataContainer);
        }

        return compoundtag;
    }

    private static void saveTicks(CompoundTag levelData, PackedTicks ticksForSerialization) {
        levelData.store("block_ticks", BLOCK_TICKS_CODEC, ticksForSerialization.blocks());
        levelData.store("fluid_ticks", FLUID_TICKS_CODEC, ticksForSerialization.fluids());
    }

    public static ChunkStatus getChunkStatusFromTag(@Nullable CompoundTag tag) {
        return tag != null ? (ChunkStatus) tag.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY) : ChunkStatus.EMPTY;
    }

    @Nullable
    private static PostLoadProcessor postLoadChunk(ServerLevel level, List<CompoundTag> entities, List<CompoundTag> blockEntities) {
        return entities.isEmpty() && blockEntities.isEmpty() ? null : levelchunk -> {
            level.timings.syncChunkLoadEntitiesTimer.startTiming();
            if (!entities.isEmpty()) {
                ScopedCollector problemreporter_scopedcollector = new ScopedCollector(levelchunk.problemPath(), LOGGER);

                try {
                    level.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(
                        TagValueInput.create(problemreporter_scopedcollector, level.registryAccess(), entities), level,
                        EntitySpawnReason.LOAD));
                } catch (Throwable var10) {
                    try {
                        problemreporter_scopedcollector.close();
                    } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                    }

                    throw var10;
                }

                problemreporter_scopedcollector.close();
            }

            level.timings.syncChunkLoadEntitiesTimer.stopTiming();
            level.timings.syncChunkLoadTileEntitiesTimer.startTiming();

            for (CompoundTag compoundtag : blockEntities) {
                boolean flag = compoundtag.getBooleanOr("keepPacked", false);
                if (flag) {
                    levelchunk.setBlockEntityNbt(compoundtag);
                } else {
                    BlockPos blockpos = BlockEntity.getPosFromTag(levelchunk.getPos(), compoundtag);
                    BlockEntity blockentity = BlockEntity.loadStatic(blockpos, levelchunk.getBlockState(blockpos), compoundtag,
                        level.registryAccess());
                    if (blockentity != null) {
                        levelchunk.setBlockEntity(blockentity);
                    }
                }
            }

            level.timings.syncChunkLoadTileEntitiesTimer.stopTiming();
        };
    }

    private static CompoundTag packStructureData(StructurePieceSerializationContext context, ChunkPos pos,
        Map<Structure, StructureStart> starts, Map<Structure, LongSet> references) {
        CompoundTag compoundtag = new CompoundTag();
        CompoundTag compoundtag1 = new CompoundTag();
        Registry<Structure> registry = context.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        for (Entry<Structure, StructureStart> map_entry : starts.entrySet()) {
            Identifier identifier = registry.getKey((Structure) map_entry.getKey());
            compoundtag1.put(identifier.toString(), ((StructureStart) map_entry.getValue()).createTag(context, pos));
        }

        compoundtag.put("starts", compoundtag1);
        CompoundTag compoundtag2 = new CompoundTag();

        for (Entry<Structure, LongSet> map_entry1 : references.entrySet()) {
            if (!((LongSet) map_entry1.getValue()).isEmpty()) {
                Identifier identifier1 = registry.getKey((Structure) map_entry1.getKey());
                compoundtag2.putLongArray(identifier1.toString(), ((LongSet) map_entry1.getValue()).toLongArray());
            }
        }

        compoundtag.put("References", compoundtag2);
        return compoundtag;
    }

    private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext context, CompoundTag tag,
        long seed) {
        Map<Structure, StructureStart> map = Maps.newHashMap();
        Registry<Structure> registry = context.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        CompoundTag compoundtag1 = tag.getCompoundOrEmpty("starts");

        for (String s : compoundtag1.keySet()) {
            Identifier identifier = Identifier.tryParse(s);
            Structure structure = (Structure) registry.getValue(identifier);
            if (structure == null) {
                LOGGER.error("Unknown structure start: {}", identifier);
            } else {
                StructureStart structurestart = StructureStart.loadStaticStart(context, compoundtag1.getCompoundOrEmpty(s), seed);
                if (structurestart != null) {
                    Tag persistentBase = compoundtag1.getCompoundOrEmpty(s).get("StructureBukkitValues");
                    if (persistentBase instanceof CompoundTag) {
                        structurestart.persistentDataContainer.putAll((CompoundTag) persistentBase);
                    }

                    map.put(structure, structurestart);
                }
            }
        }

        return map;
    }

    private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess registryAccess, ChunkPos pos, CompoundTag tag) {
        Map<Structure, LongSet> map = Maps.newHashMap();
        Registry<Structure> registry = registryAccess.lookupOrThrow(Registries.STRUCTURE);
        CompoundTag compoundtag1 = tag.getCompoundOrEmpty("References");
        compoundtag1.forEach((s, tag1) -> {
            Identifier identifier = Identifier.tryParse(s);
            Structure structure = (Structure) registry.getValue(identifier);
            if (structure == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", identifier, pos);
            } else {
                Optional<long[]> optional = tag1.asLongArray();
                if (!optional.isEmpty()) {
                    map.put(structure, new LongOpenHashSet(Arrays.stream((long[]) optional.get()).filter(i -> {
                        ChunkPos chunkpos1 = ChunkPos.unpack(i);
                        if (chunkpos1.getChessboardDistance(pos) > 8) {
                            LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", new Object[] {
                                identifier,
                                chunkpos1,
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
        return map;
    }

    private static ListTag packOffsets(ShortList[] sections) {
        ListTag listtag = new ListTag();

        for (ShortList shortlist : sections) {
            ListTag listtag1 = new ListTag();
            if (shortlist != null) {
                for (int i = 0; i < shortlist.size(); ++i) {
                    listtag1.add(ShortTag.valueOf(shortlist.getShort(i)));
                }
            }

            listtag.add(listtag1);
        }

        return listtag;
    }

    public static class ChunkReadException extends NbtException {
        public ChunkReadException(String message) {
            super(message);
        }
    }

    public static record SectionData(int y, @Nullable LevelChunkSection chunkSection, @Nullable DataLayer blockLight,
        @Nullable DataLayer skyLight) {}
}
