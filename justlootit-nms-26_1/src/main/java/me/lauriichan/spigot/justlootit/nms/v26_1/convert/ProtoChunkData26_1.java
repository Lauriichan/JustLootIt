package me.lauriichan.spigot.justlootit.nms.v26_1.convert;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import me.lauriichan.spigot.justlootit.nms.v26_1.util.PlatformHelper26_1;

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
@SuppressWarnings({"unused", "unchecked", "rawtypes", "serial"})
public record ProtoChunkData26_1(PalettedContainerFactory containerFactory, ChunkPos chunkPos, int minSectionY, long lastUpdateTime,
    long inhabitedTime, ChunkStatus chunkStatus, @Nullable Packed blendingData, @Nullable BelowZeroRetrogen belowZeroRetrogen,
    UpgradeData upgradeData, @Nullable long[] carvingMask, Map<Types, long[]> heightmaps, PackedTicks packedTicks,
    ShortList[] postProcessingSections, boolean lightCorrect, List<ProtoChunkData26_1.SectionData> sectionData,
    List<CompoundTag> entities, List<CompoundTag> blockEntities, CompoundTag structureData, @Nullable Tag persistentDataContainer) {
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

    public static ProtoChunkData26_1 parse(LevelHeightAccessor levelheightaccessor, PalettedContainerFactory palettedcontainerfactory,
        CompoundTag nbttagcompound) {
        if (nbttagcompound.getString("Status").isEmpty()) {
            return null;
        } else {
            ChunkPos chunkcoordintpair = new ChunkPos(nbttagcompound.getIntOr("xPos", 0), nbttagcompound.getIntOr("zPos", 0));
            long i = nbttagcompound.getLongOr("LastUpdate", 0L);
            long j = nbttagcompound.getLongOr("InhabitedTime", 0L);
            ChunkStatus chunkstatus = (ChunkStatus) nbttagcompound.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY);
            UpgradeData chunkconverter = (UpgradeData) nbttagcompound.getCompound("UpgradeData")
                .map(nbttagcompound1x -> new UpgradeData(nbttagcompound1x, levelheightaccessor)).orElse(UpgradeData.EMPTY);
            boolean flag = nbttagcompound.getBooleanOr("isLightOn", false);
            Packed blendingdata_d = (Packed) nbttagcompound.read("blending_data", Packed.CODEC).orElse(null);
            BelowZeroRetrogen belowzeroretrogen = (BelowZeroRetrogen) nbttagcompound.read("below_zero_retrogen", BelowZeroRetrogen.CODEC)
                .orElse(null);
            long[] along = (long[]) nbttagcompound.getLongArray("carving_mask").orElse(null);
            Map<Types, long[]> map = new EnumMap(Types.class);
            nbttagcompound.getCompound("Heightmaps").ifPresent(nbttagcompound1x -> {
                for (Types heightmap_type : chunkstatus.heightmapsAfter()) {
                    nbttagcompound1x.getLongArray(heightmap_type.getSerializationKey())
                        .ifPresent(along1 -> map.put(heightmap_type, along1));
                }
            });
            List<SavedTick<Block>> list = SavedTick
                .filterTickListForChunk((List) nbttagcompound.read("block_ticks", BLOCK_TICKS_CODEC).orElse(List.of()), chunkcoordintpair);
            List<SavedTick<Fluid>> list1 = SavedTick
                .filterTickListForChunk((List) nbttagcompound.read("fluid_ticks", FLUID_TICKS_CODEC).orElse(List.of()), chunkcoordintpair);
            PackedTicks ichunkaccess_b = new PackedTicks(list, list1);
            ListTag nbttaglist = nbttagcompound.getListOrEmpty("PostProcessing");
            ShortList[] ashortlist = new ShortList[nbttaglist.size()];

            for (int k = 0; k < nbttaglist.size(); ++k) {
                ListTag nbttaglist1 = (ListTag) nbttaglist.getList(k).orElse(null);
                if (nbttaglist1 != null && !nbttaglist1.isEmpty()) {
                    ShortList shortlist = new ShortArrayList(nbttaglist1.size());

                    for (int l = 0; l < nbttaglist1.size(); ++l) {
                        shortlist.add(nbttaglist1.getShortOr(l, (short) 0));
                    }

                    ashortlist[k] = shortlist;
                }
            }

            List<CompoundTag> list2 = nbttagcompound.getList("entities").stream().flatMap(ListTag::compoundStream).collect(ObjectArrayList.toList());
            List<CompoundTag> list3 = nbttagcompound.getList("block_entities").stream().flatMap(ListTag::compoundStream).collect(ObjectArrayList.toList());
            CompoundTag nbttagcompound1 = nbttagcompound.getCompoundOrEmpty("structures");
            ListTag nbttaglist2 = nbttagcompound.getListOrEmpty("sections");
            List<ProtoChunkData26_1.SectionData> list4 = new ArrayList(nbttaglist2.size());
            Codec<PalettedContainer<Holder<Biome>>> codec = PlatformHelper26_1.biomeContainerCodecRW(palettedcontainerfactory);
            Codec<PalettedContainer<BlockState>> codec1 = palettedcontainerfactory.blockStatesContainerCodec();

            for (int i1 = 0; i1 < nbttaglist2.size(); ++i1) {
                Optional<CompoundTag> optional = nbttaglist2.getCompound(i1);
                if (!optional.isEmpty()) {
                    CompoundTag nbttagcompound2 = (CompoundTag) optional.get();
                    int j1 = nbttagcompound2.getByteOr("Y", (byte) 0);
                    LevelChunkSection chunksection;
                    if (j1 >= levelheightaccessor.getMinSectionY() && j1 <= levelheightaccessor.getMaxSectionY()) {
                        Optional optional1 = nbttagcompound2.getCompound("block_states")
                            .map(nbttagcompound3 -> (PalettedContainer) codec1.parse(NbtOps.INSTANCE, nbttagcompound3)
                                .promotePartial(s -> logErrors(chunkcoordintpair, j1, s))
                                .getOrThrow(ProtoChunkData26_1.ChunkReadException::new));
                        PalettedContainer<BlockState> datapaletteblock = (PalettedContainer) optional1
                            .orElseGet(palettedcontainerfactory::createForBlockStates);
                        optional1 = nbttagcompound2.getCompound("biomes")
                            .map(nbttagcompound3 -> (PalettedContainerRO) codec.parse(NbtOps.INSTANCE, nbttagcompound3)
                                .promotePartial(s -> logErrors(chunkcoordintpair, j1, s))
                                .getOrThrow(ProtoChunkData26_1.ChunkReadException::new));
                        PalettedContainer<Holder<Biome>> palettedcontainerro = (PalettedContainer) optional1
                            .orElseGet(palettedcontainerfactory::createForBiomes);
                        chunksection = new LevelChunkSection(datapaletteblock, palettedcontainerro);
                    } else {
                        chunksection = null;
                    }

                    DataLayer nibblearray = (DataLayer) nbttagcompound2.getByteArray("BlockLight").map(DataLayer::new).orElse(null);
                    DataLayer nibblearray1 = (DataLayer) nbttagcompound2.getByteArray("SkyLight").map(DataLayer::new).orElse(null);
                    list4.add(new ProtoChunkData26_1.SectionData(j1, chunksection, nibblearray, nibblearray1));
                }
            }

            return new ProtoChunkData26_1(palettedcontainerfactory, chunkcoordintpair, levelheightaccessor.getMinSectionY(), i, j,
                chunkstatus, blendingdata_d, belowzeroretrogen, chunkconverter, along, map, ichunkaccess_b, ashortlist, flag, list4, list2,
                list3, nbttagcompound1, nbttagcompound.get("ChunkBukkitValues"));
        }
    }

    public ProtoChunk read(ServerLevel worldserver, PoiManager villageplace, RegionStorageInfo regionstorageinfo,
        ChunkPos chunkcoordintpair) {
        if (!Objects.equals(chunkcoordintpair, this.chunkPos)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", new Object[] {
                chunkcoordintpair,
                chunkcoordintpair,
                this.chunkPos
            });
            worldserver.getServer().reportMisplacedChunk(this.chunkPos, chunkcoordintpair, regionstorageinfo);
        }

        int i = worldserver.getSectionsCount();
        LevelChunkSection[] achunksection = new LevelChunkSection[i];
        boolean flag = worldserver.dimensionType().hasSkyLight();
        ChunkSource ichunkprovider = worldserver.getChunkSource();
        LevelLightEngine levellightengine = ichunkprovider.getLightEngine();
        PalettedContainerFactory palettedcontainerfactory = worldserver.palettedContainerFactory();
        boolean flag1 = false;

        for (ProtoChunkData26_1.SectionData ProtoChunkData_b : this.sectionData) {
            SectionPos sectionposition = SectionPos.of(chunkcoordintpair, ProtoChunkData_b.y);
            if (ProtoChunkData_b.chunkSection != null) {
                achunksection[worldserver.getSectionIndexFromSectionY(ProtoChunkData_b.y)] = ProtoChunkData_b.chunkSection;
                villageplace.checkConsistencyWithBlocks(sectionposition, ProtoChunkData_b.chunkSection);
            }

            boolean flag2 = ProtoChunkData_b.blockLight != null;
            boolean flag3 = flag && ProtoChunkData_b.skyLight != null;
            if (flag2 || flag3) {
                if (!flag1) {
                    levellightengine.retainData(chunkcoordintpair, true);
                    flag1 = true;
                }

                if (flag2) {
                    levellightengine.queueSectionData(LightLayer.BLOCK, sectionposition, ProtoChunkData_b.blockLight);
                }

                if (flag3) {
                    levellightengine.queueSectionData(LightLayer.SKY, sectionposition, ProtoChunkData_b.skyLight);
                }
            }
        }

        ChunkType chunktype = this.chunkStatus.getChunkType();
        ChunkAccess ichunkaccess;
        if (chunktype == ChunkType.LEVELCHUNK) {
            LevelChunkTicks<Block> levelchunkticks = new LevelChunkTicks(this.packedTicks.blocks());
            LevelChunkTicks<Fluid> levelchunkticks1 = new LevelChunkTicks(this.packedTicks.fluids());
            ichunkaccess = new LevelChunk(worldserver.getLevel(), chunkcoordintpair, this.upgradeData, levelchunkticks, levelchunkticks1,
                this.inhabitedTime, achunksection, postLoadChunk(worldserver, this.entities, this.blockEntities),
                BlendingData.unpack(this.blendingData));
        } else {
            ProtoChunkTicks<Block> protochunkticklist = ProtoChunkTicks.load(this.packedTicks.blocks());
            ProtoChunkTicks<Fluid> protochunkticklist1 = ProtoChunkTicks.load(this.packedTicks.fluids());
            ProtoChunk protochunk = new ProtoChunk(chunkcoordintpair, this.upgradeData, achunksection, protochunkticklist,
                protochunkticklist1, worldserver, palettedcontainerfactory, BlendingData.unpack(this.blendingData));
            ichunkaccess = protochunk;
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
            ichunkaccess.persistentDataContainer.putAll((CompoundTag) this.persistentDataContainer);
        }

        ichunkaccess.setLightCorrect(this.lightCorrect);
        EnumSet<Types> enumset = EnumSet.noneOf(Types.class);

        for (Types heightmap_type : ichunkaccess.getPersistedStatus().heightmapsAfter()) {
            long[] along = (long[]) this.heightmaps.get(heightmap_type);
            if (along != null) {
                ichunkaccess.setHeightmap(heightmap_type, along);
            } else {
                enumset.add(heightmap_type);
            }
        }

        Heightmap.primeHeightmaps(ichunkaccess, enumset);
        ichunkaccess.setAllStarts(
            unpackStructureStart(StructurePieceSerializationContext.fromLevel(worldserver), this.structureData, worldserver.getSeed()));
        ichunkaccess.setAllReferences(unpackStructureReferences(worldserver.registryAccess(), chunkcoordintpair, this.structureData));

        for (int j = 0; j < this.postProcessingSections.length; ++j) {
            ShortList shortlist = this.postProcessingSections[j];
            if (shortlist != null) {
                ichunkaccess.addPackedPostProcess(shortlist, j);
            }
        }

        if (chunktype == ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk) ichunkaccess, false);
        } else {
            ProtoChunk protochunk1 = (ProtoChunk) ichunkaccess;

            for (CompoundTag nbttagcompound : this.entities) {
                protochunk1.addEntity(nbttagcompound);
            }

            for (CompoundTag nbttagcompound1 : this.blockEntities) {
                protochunk1.setBlockEntityNbt(nbttagcompound1);
            }

            if (this.carvingMask != null) {
                protochunk1.setCarvingMask(new CarvingMask(this.carvingMask, ichunkaccess.getMinY()));
            }

            return protochunk1;
        }
    }

    private static void logErrors(ChunkPos chunkcoordintpair, int i, String s) {
        LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", new Object[] {
            chunkcoordintpair.x(),
            i,
            chunkcoordintpair.z(),
            s
        });
    }

    public static ProtoChunkData26_1 copyOf(ServerLevel worldserver, ChunkAccess ichunkaccess) {
        if (!ichunkaccess.canBeSerialized()) {
            throw new IllegalArgumentException("Chunk can't be serialized: " + ichunkaccess);
        } else {
            ChunkPos chunkcoordintpair = ichunkaccess.getPos();
            List<ProtoChunkData26_1.SectionData> list = new ArrayList();
            LevelChunkSection[] achunksection = ichunkaccess.getSections();
            LevelLightEngine levellightengine = worldserver.getChunkSource().getLightEngine();

            for (int i = levellightengine.getMinLightSection(); i < levellightengine.getMaxLightSection(); ++i) {
                int j = ichunkaccess.getSectionIndexFromSectionY(i);
                boolean flag = j >= 0 && j < achunksection.length;
                DataLayer nibblearray = levellightengine.getLayerListener(LightLayer.BLOCK)
                    .getDataLayerData(SectionPos.of(chunkcoordintpair, i));
                DataLayer nibblearray1 = levellightengine.getLayerListener(LightLayer.SKY)
                    .getDataLayerData(SectionPos.of(chunkcoordintpair, i));
                DataLayer nibblearray2 = nibblearray != null && !nibblearray.isEmpty() ? nibblearray.copy() : null;
                DataLayer nibblearray3 = nibblearray1 != null && !nibblearray1.isEmpty() ? nibblearray1.copy() : null;
                if (flag || nibblearray2 != null || nibblearray3 != null) {
                    LevelChunkSection chunksection = flag ? achunksection[j].copy() : null;
                    list.add(new ProtoChunkData26_1.SectionData(i, chunksection, nibblearray2, nibblearray3));
                }
            }

            List<CompoundTag> list1 = new ArrayList(ichunkaccess.getBlockEntitiesPos().size());

            for (BlockPos blockposition : ichunkaccess.getBlockEntitiesPos()) {
                CompoundTag nbttagcompound = ichunkaccess.getBlockEntityNbtForSaving(blockposition, worldserver.registryAccess());
                if (nbttagcompound != null) {
                    list1.add(nbttagcompound);
                }
            }

            List<CompoundTag> list2 = new ArrayList();
            long[] along = null;
            if (ichunkaccess.getPersistedStatus().getChunkType() == ChunkType.PROTOCHUNK) {
                ProtoChunk protochunk = (ProtoChunk) ichunkaccess;
                list2.addAll(protochunk.getEntities());
                CarvingMask carvingmask = protochunk.getCarvingMask();
                if (carvingmask != null) {
                    along = carvingmask.toArray();
                }
            }

            Map<Types, long[]> map = new EnumMap(Types.class);

            for (Entry<Types, Heightmap> map_entry : ichunkaccess.getHeightmaps()) {
                if (ichunkaccess.getPersistedStatus().heightmapsAfter().contains(map_entry.getKey())) {
                    long[] along1 = ((Heightmap) map_entry.getValue()).getRawData();
                    map.put((Types) map_entry.getKey(), (long[]) along1.clone());
                }
            }

            PackedTicks ichunkaccess_b = ichunkaccess.getTicksForSerialization(worldserver.getGameTime());
            ShortList[] ashortlist = (ShortList[]) Arrays.stream(ichunkaccess.getPostProcessing())
                .map(shortlist -> shortlist != null && !shortlist.isEmpty() ? new ShortArrayList(shortlist) : null)
                .toArray(k -> new ShortList[k]);
            CompoundTag nbttagcompound1 = packStructureData(StructurePieceSerializationContext.fromLevel(worldserver), chunkcoordintpair,
                ichunkaccess.getAllStarts(), ichunkaccess.getAllReferences());
            CompoundTag persistentDataContainer = null;
            if (!ichunkaccess.persistentDataContainer.isEmpty()) {
                persistentDataContainer = ichunkaccess.persistentDataContainer.toTagCompound();
            }

            return new ProtoChunkData26_1(worldserver.palettedContainerFactory(), chunkcoordintpair, ichunkaccess.getMinSectionY(),
                worldserver.getGameTime(), ichunkaccess.getInhabitedTime(), ichunkaccess.getPersistedStatus(),
                (Packed) Optionull.map(ichunkaccess.getBlendingData(), BlendingData::pack), ichunkaccess.getBelowZeroRetrogen(),
                ichunkaccess.getUpgradeData().copy(), along, map, ichunkaccess_b, ashortlist, ichunkaccess.isLightCorrect(), list, list2,
                list1, nbttagcompound1, persistentDataContainer);
        }
    }

    public CompoundTag write() {
        CompoundTag nbttagcompound = NbtUtils.addCurrentDataVersion(new CompoundTag());
        nbttagcompound.putInt("xPos", this.chunkPos.x());
        nbttagcompound.putInt("yPos", this.minSectionY);
        nbttagcompound.putInt("zPos", this.chunkPos.z());
        nbttagcompound.putLong("LastUpdate", this.lastUpdateTime);
        nbttagcompound.putLong("InhabitedTime", this.inhabitedTime);
        nbttagcompound.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(this.chunkStatus).toString());
        nbttagcompound.storeNullable("blending_data", Packed.CODEC, this.blendingData);
        nbttagcompound.storeNullable("below_zero_retrogen", BelowZeroRetrogen.CODEC, this.belowZeroRetrogen);
        if (!this.upgradeData.isEmpty()) {
            nbttagcompound.put("UpgradeData", this.upgradeData.write());
        }

        ListTag nbttaglist = new ListTag();
        Codec<PalettedContainer<BlockState>> codec = this.containerFactory.blockStatesContainerCodec();
        Codec<PalettedContainerRO<Holder<Biome>>> codec1 = this.containerFactory.biomeContainerCodec();

        for (ProtoChunkData26_1.SectionData ProtoChunkData_b : this.sectionData) {
            CompoundTag nbttagcompound1 = new CompoundTag();
            LevelChunkSection chunksection = ProtoChunkData_b.chunkSection;
            if (chunksection != null) {
                nbttagcompound1.store("block_states", codec, chunksection.getStates());
                nbttagcompound1.store("biomes", codec1, chunksection.getBiomes());
            }

            if (ProtoChunkData_b.blockLight != null) {
                nbttagcompound1.putByteArray("BlockLight", ProtoChunkData_b.blockLight.getData());
            }

            if (ProtoChunkData_b.skyLight != null) {
                nbttagcompound1.putByteArray("SkyLight", ProtoChunkData_b.skyLight.getData());
            }

            if (!nbttagcompound1.isEmpty()) {
                nbttagcompound1.putByte("Y", (byte) ProtoChunkData_b.y);
                nbttaglist.add(nbttagcompound1);
            }
        }

        nbttagcompound.put("sections", nbttaglist);
        if (this.lightCorrect) {
            nbttagcompound.putBoolean("isLightOn", true);
        }

        ListTag nbttaglist1 = new ListTag();
        nbttaglist1.addAll(this.blockEntities);
        nbttagcompound.put("block_entities", nbttaglist1);
        if (this.chunkStatus.getChunkType() == ChunkType.PROTOCHUNK) {
            ListTag nbttaglist2 = new ListTag();
            nbttaglist2.addAll(this.entities);
            nbttagcompound.put("entities", nbttaglist2);
            if (this.carvingMask != null) {
                nbttagcompound.putLongArray("carving_mask", this.carvingMask);
            }
        }

        saveTicks(nbttagcompound, this.packedTicks);
        nbttagcompound.put("PostProcessing", packOffsets(this.postProcessingSections));
        CompoundTag nbttagcompound2 = new CompoundTag();
        this.heightmaps
            .forEach((heightmap_type, along) -> nbttagcompound2.put(heightmap_type.getSerializationKey(), new LongArrayTag(along)));
        nbttagcompound.put("Heightmaps", nbttagcompound2);
        nbttagcompound.put("structures", this.structureData);
        if (this.persistentDataContainer != null) {
            nbttagcompound.put("ChunkBukkitValues", this.persistentDataContainer);
        }

        return nbttagcompound;
    }

    private static void saveTicks(CompoundTag nbttagcompound, PackedTicks ichunkaccess_b) {
        nbttagcompound.store("block_ticks", BLOCK_TICKS_CODEC, ichunkaccess_b.blocks());
        nbttagcompound.store("fluid_ticks", FLUID_TICKS_CODEC, ichunkaccess_b.fluids());
    }

    public static ChunkStatus getChunkStatusFromTag(@Nullable CompoundTag nbttagcompound) {
        return nbttagcompound != null ? (ChunkStatus) nbttagcompound.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY)
            : ChunkStatus.EMPTY;
    }

    @Nullable
    private static PostLoadProcessor postLoadChunk(ServerLevel worldserver, List<CompoundTag> list, List<CompoundTag> list1) {
        return list.isEmpty() && list1.isEmpty() ? null : chunk -> {
            worldserver.timings.syncChunkLoadEntitiesTimer.startTiming();
            if (!list.isEmpty()) {
                ScopedCollector problemreporter_j = new ScopedCollector(chunk.problemPath(), LOGGER);

                try {
                    worldserver.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(
                        TagValueInput.create(problemreporter_j, worldserver.registryAccess(), list), worldserver, EntitySpawnReason.LOAD));
                } catch (Throwable var10) {
                    try {
                        problemreporter_j.close();
                    } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                    }

                    throw var10;
                }

                problemreporter_j.close();
            }

            worldserver.timings.syncChunkLoadEntitiesTimer.stopTiming();
            worldserver.timings.syncChunkLoadTileEntitiesTimer.startTiming();

            for (CompoundTag nbttagcompound : list1) {
                boolean flag = nbttagcompound.getBooleanOr("keepPacked", false);
                if (flag) {
                    chunk.setBlockEntityNbt(nbttagcompound);
                } else {
                    BlockPos blockposition = BlockEntity.getPosFromTag(chunk.getPos(), nbttagcompound);
                    BlockEntity tileentity = BlockEntity.loadStatic(blockposition, chunk.getBlockState(blockposition), nbttagcompound,
                        worldserver.registryAccess());
                    if (tileentity != null) {
                        chunk.setBlockEntity(tileentity);
                    }
                }
            }

            worldserver.timings.syncChunkLoadTileEntitiesTimer.stopTiming();
        };
    }

    private static CompoundTag packStructureData(StructurePieceSerializationContext structurepieceserializationcontext,
        ChunkPos chunkcoordintpair, Map<Structure, StructureStart> map, Map<Structure, LongSet> map1) {
        CompoundTag nbttagcompound = new CompoundTag();
        CompoundTag nbttagcompound1 = new CompoundTag();
        Registry<Structure> iregistry = structurepieceserializationcontext.registryAccess().lookupOrThrow(Registries.STRUCTURE);

        for (Entry<Structure, StructureStart> map_entry : map.entrySet()) {
            Identifier minecraftkey = iregistry.getKey((Structure) map_entry.getKey());
            nbttagcompound1.put(minecraftkey.toString(),
                ((StructureStart) map_entry.getValue()).createTag(structurepieceserializationcontext, chunkcoordintpair));
        }

        nbttagcompound.put("starts", nbttagcompound1);
        CompoundTag nbttagcompound2 = new CompoundTag();

        for (Entry<Structure, LongSet> map_entry1 : map1.entrySet()) {
            if (!((LongSet) map_entry1.getValue()).isEmpty()) {
                Identifier minecraftkey1 = iregistry.getKey((Structure) map_entry1.getKey());
                nbttagcompound2.putLongArray(minecraftkey1.toString(), ((LongSet) map_entry1.getValue()).toLongArray());
            }
        }

        nbttagcompound.put("References", nbttagcompound2);
        return nbttagcompound;
    }

    private static Map<Structure, StructureStart> unpackStructureStart(
        StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag nbttagcompound, long i) {
        Map<Structure, StructureStart> map = Maps.newHashMap();
        Registry<Structure> iregistry = structurepieceserializationcontext.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        CompoundTag nbttagcompound1 = nbttagcompound.getCompoundOrEmpty("starts");

        for (String s : nbttagcompound1.keySet()) {
            Identifier minecraftkey = Identifier.tryParse(s);
            Structure structure = (Structure) iregistry.getValue(minecraftkey);
            if (structure == null) {
                LOGGER.error("Unknown structure start: {}", minecraftkey);
            } else {
                StructureStart structurestart = StructureStart.loadStaticStart(structurepieceserializationcontext,
                    nbttagcompound1.getCompoundOrEmpty(s), i);
                if (structurestart != null) {
                    Tag persistentBase = nbttagcompound1.getCompoundOrEmpty(s).get("StructureBukkitValues");
                    if (persistentBase instanceof CompoundTag) {
                        structurestart.persistentDataContainer.putAll((CompoundTag) persistentBase);
                    }

                    map.put(structure, structurestart);
                }
            }
        }

        return map;
    }

    private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess iregistrycustom, ChunkPos chunkcoordintpair,
        CompoundTag nbttagcompound) {
        Map<Structure, LongSet> map = Maps.newHashMap();
        Registry<Structure> iregistry = iregistrycustom.lookupOrThrow(Registries.STRUCTURE);
        CompoundTag nbttagcompound1 = nbttagcompound.getCompoundOrEmpty("References");
        nbttagcompound1.forEach((s, nbtbase) -> {
            Identifier minecraftkey = Identifier.tryParse(s);
            Structure structure = (Structure) iregistry.getValue(minecraftkey);
            if (structure == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", minecraftkey, chunkcoordintpair);
            } else {
                Optional<long[]> optional = nbtbase.asLongArray();
                if (!optional.isEmpty()) {
                    map.put(structure, new LongOpenHashSet(Arrays.stream((long[]) optional.get()).filter(i -> {
                        ChunkPos chunkcoordintpair1 = ChunkPos.unpack(i);
                        if (chunkcoordintpair1.getChessboardDistance(chunkcoordintpair) > 8) {
                            LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", new Object[] {
                                minecraftkey,
                                chunkcoordintpair1,
                                chunkcoordintpair
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

    private static ListTag packOffsets(ShortList[] ashortlist) {
        ListTag nbttaglist = new ListTag();

        for (ShortList shortlist : ashortlist) {
            ListTag nbttaglist1 = new ListTag();
            if (shortlist != null) {
                for (int i = 0; i < shortlist.size(); ++i) {
                    nbttaglist1.add(ShortTag.valueOf(shortlist.getShort(i)));
                }
            }

            nbttaglist.add(nbttaglist1);
        }

        return nbttaglist;
    }

    public static class ChunkReadException extends NbtException {
        public ChunkReadException(String s) {
            super(s);
        }
    }

    public static record SectionData(int y, @Nullable LevelChunkSection chunkSection, @Nullable DataLayer blockLight,
        @Nullable DataLayer skyLight) {}
}
