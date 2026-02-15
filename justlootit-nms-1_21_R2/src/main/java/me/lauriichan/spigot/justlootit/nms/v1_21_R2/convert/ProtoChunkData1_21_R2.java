package me.lauriichan.spigot.justlootit.nms.v1_21_R2.convert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.annotation.Nullable;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.ChunkAccess.PackedTicks;
import net.minecraft.world.level.chunk.LevelChunk.PostLoadProcessor;
import net.minecraft.world.level.chunk.PalettedContainer.Strategy;
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
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import org.slf4j.Logger;

/**
 * Copy of {@link SerializableChunkData} in Spigot
 */
@SuppressWarnings({"unused", "unchecked", "rawtypes", "serial"})
public record ProtoChunkData1_21_R2(
   Registry<Biome> biomeRegistry,
   ChunkPos chunkPos,
   int minSectionY,
   long lastUpdateTime,
   long inhabitedTime,
   ChunkStatus chunkStatus,
   @Nullable Packed blendingData,
   @Nullable BelowZeroRetrogen belowZeroRetrogen,
   UpgradeData upgradeData,
   @Nullable long[] carvingMask,
   Map<Types, long[]> heightmaps,
   PackedTicks packedTicks,
   ShortList[] postProcessingSections,
   boolean lightCorrect,
   List<ProtoChunkData1_21_R2.SectionData> sectionData,
   List<CompoundTag> entities,
   List<CompoundTag> blockEntities,
   CompoundTag structureData,
   @Nullable Tag persistentDataContainer
) {
   public static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(
      Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState()
   );
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

   @Nullable
   public static ProtoChunkData1_21_R2 parse(LevelHeightAccessor levelheightaccessor, RegistryAccess iregistrycustom, CompoundTag nbttagcompound) {
      if (!nbttagcompound.contains("Status", 8)) {
         return null;
      } else {
         ChunkPos chunkcoordintpair = new ChunkPos(nbttagcompound.getInt("xPos"), nbttagcompound.getInt("zPos"));
         long i = nbttagcompound.getLong("LastUpdate");
         long j = nbttagcompound.getLong("InhabitedTime");
         ChunkStatus chunkstatus = ChunkStatus.byName(nbttagcompound.getString("Status"));
         UpgradeData chunkconverter = nbttagcompound.contains("UpgradeData", 10)
            ? new UpgradeData(nbttagcompound.getCompound("UpgradeData"), levelheightaccessor)
            : UpgradeData.EMPTY;
         boolean flag = nbttagcompound.getBoolean("isLightOn");
         Packed blendingdata_d;
         if (nbttagcompound.contains("blending_data", 10)) {
            DataResult<Packed> dataresult = Packed.CODEC.parse(NbtOps.INSTANCE, nbttagcompound.getCompound("blending_data"));
            Logger logger = LOGGER;
            blendingdata_d = dataresult.resultOrPartial(logger::error).orElse(null);
         } else {
            blendingdata_d = null;
         }

         BelowZeroRetrogen belowzeroretrogen;
         if (nbttagcompound.contains("below_zero_retrogen", 10)) {
            DataResult<BelowZeroRetrogen> dataresult = BelowZeroRetrogen.CODEC.parse(NbtOps.INSTANCE, nbttagcompound.getCompound("below_zero_retrogen"));
            Logger logger = LOGGER;
            belowzeroretrogen = dataresult.resultOrPartial(logger::error).orElse(null);
         } else {
            belowzeroretrogen = null;
         }

         long[] along;
         if (nbttagcompound.contains("carving_mask", 12)) {
            along = nbttagcompound.getLongArray("carving_mask");
         } else {
            along = null;
         }

         CompoundTag nbttagcompound1 = nbttagcompound.getCompound("Heightmaps");
         Map<Types, long[]> map = new EnumMap(Types.class);

         for(Types heightmap_type : chunkstatus.heightmapsAfter()) {
            String s = heightmap_type.getSerializationKey();
            if (nbttagcompound1.contains(s, 12)) {
               map.put(heightmap_type, nbttagcompound1.getLongArray(s));
            }
         }

         List<SavedTick<Block>> list = SavedTick.loadTickList(
            nbttagcompound.getList("block_ticks", 10), s1 -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(s1)), chunkcoordintpair
         );
         List<SavedTick<Fluid>> list1 = SavedTick.loadTickList(
            nbttagcompound.getList("fluid_ticks", 10), s1 -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(s1)), chunkcoordintpair
         );
         PackedTicks ichunkaccess_a = new PackedTicks(list, list1);
         ListTag nbttaglist = nbttagcompound.getList("PostProcessing", 9);
         ShortList[] ashortlist = new ShortList[nbttaglist.size()];

         for(int k = 0; k < nbttaglist.size(); ++k) {
            ListTag nbttaglist1 = nbttaglist.getList(k);
            ShortArrayList shortarraylist = new ShortArrayList(nbttaglist1.size());

            for(int l = 0; l < nbttaglist1.size(); ++l) {
               shortarraylist.add(nbttaglist1.getShort(l));
            }

            ashortlist[k] = shortarraylist;
         }

         List<CompoundTag> list2 = compoundStream(nbttagcompound.getList("entities", 10)).collect(ObjectArrayList.toList());
         List<CompoundTag> list3 = compoundStream(nbttagcompound.getList("block_entities", 10)).collect(ObjectArrayList.toList());
         CompoundTag nbttagcompound2 = nbttagcompound.getCompound("structures");
         ListTag nbttaglist2 = nbttagcompound.getList("sections", 10);
         List<ProtoChunkData1_21_R2.SectionData> list4 = new ArrayList(nbttaglist2.size());
         Registry<Biome> iregistry = iregistrycustom.lookupOrThrow(Registries.BIOME);
         Codec<PalettedContainer<Holder<Biome>>> codec = makeBiomeCodecRW(iregistry);

         for(int i1 = 0; i1 < nbttaglist2.size(); ++i1) {
            CompoundTag nbttagcompound3 = nbttaglist2.getCompound(i1);
            byte b0 = nbttagcompound3.getByte("Y");
            LevelChunkSection chunksection;
            if (b0 >= levelheightaccessor.getMinSectionY() && b0 <= levelheightaccessor.getMaxSectionY()) {
               PalettedContainer datapaletteblock;
               if (nbttagcompound3.contains("block_states", 10)) {
                  datapaletteblock = (PalettedContainer)BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, nbttagcompound3.getCompound("block_states"))
                     .promotePartial(s1 -> logErrors(chunkcoordintpair, b0, s1))
                     .getOrThrow(ProtoChunkData1_21_R2.ChunkReadException::new);
               } else {
                  datapaletteblock = new PalettedContainer(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), Strategy.SECTION_STATES);
               }

               PalettedContainer object;
               if (nbttagcompound3.contains("biomes", 10)) {
                  object = (PalettedContainer)codec.parse(NbtOps.INSTANCE, nbttagcompound3.getCompound("biomes"))
                     .promotePartial(s1 -> logErrors(chunkcoordintpair, b0, s1))
                     .getOrThrow(ProtoChunkData1_21_R2.ChunkReadException::new);
               } else {
                  object = new PalettedContainer(iregistry.asHolderIdMap(), iregistry.getOrThrow(Biomes.PLAINS), Strategy.SECTION_BIOMES);
               }

               chunksection = new LevelChunkSection(datapaletteblock, object);
            } else {
               chunksection = null;
            }

            DataLayer nibblearray = nbttagcompound3.contains("BlockLight", 7) ? new DataLayer(nbttagcompound3.getByteArray("BlockLight")) : null;
            DataLayer nibblearray1 = nbttagcompound3.contains("SkyLight", 7) ? new DataLayer(nbttagcompound3.getByteArray("SkyLight")) : null;
            list4.add(new ProtoChunkData1_21_R2.SectionData(b0, chunksection, nibblearray, nibblearray1));
         }

         return new ProtoChunkData1_21_R2(
            iregistry,
            chunkcoordintpair,
            levelheightaccessor.getMinSectionY(),
            i,
            j,
            chunkstatus,
            blendingdata_d,
            belowzeroretrogen,
            chunkconverter,
            along,
            map,
            ichunkaccess_a,
            ashortlist,
            flag,
            list4,
            list2,
            list3,
            nbttagcompound2,
            nbttagcompound.get("ChunkBukkitValues")
         );
      }
   }

   private static Stream<CompoundTag> compoundStream(ListTag listTag) {
      return listTag.stream().mapMulti((var0, var1) -> {
         if (var0 instanceof CompoundTag var2) {
            var1.accept(var2);
         }
      });
   }

   public ProtoChunk read(ServerLevel worldserver, PoiManager villageplace, RegionStorageInfo regionstorageinfo, ChunkPos chunkcoordintpair) {
      if (!Objects.equals(chunkcoordintpair, this.chunkPos)) {
         LOGGER.error(
            "Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", new Object[]{chunkcoordintpair, chunkcoordintpair, this.chunkPos}
         );
         worldserver.getServer().reportMisplacedChunk(this.chunkPos, chunkcoordintpair, regionstorageinfo);
      }

      int i = worldserver.getSectionsCount();
      LevelChunkSection[] achunksection = new LevelChunkSection[i];
      boolean flag = worldserver.dimensionType().hasSkyLight();
      ServerChunkCache chunkproviderserver = worldserver.getChunkSource();
      LevelLightEngine levellightengine = chunkproviderserver.getLightEngine();
      Registry<Biome> iregistry = worldserver.registryAccess().lookupOrThrow(Registries.BIOME);
      boolean flag1 = false;

      for(ProtoChunkData1_21_R2.SectionData ProtoChunkData_b : this.sectionData) {
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
      Object object;
      if (chunktype == ChunkType.LEVELCHUNK) {
         LevelChunkTicks<Block> levelchunkticks = new LevelChunkTicks(this.packedTicks.blocks());
         LevelChunkTicks<Fluid> levelchunkticks1 = new LevelChunkTicks(this.packedTicks.fluids());
         object = new LevelChunk(
            worldserver.getLevel(),
            chunkcoordintpair,
            this.upgradeData,
            levelchunkticks,
            levelchunkticks1,
            this.inhabitedTime,
            achunksection,
            postLoadChunk(worldserver, this.entities, this.blockEntities),
            BlendingData.unpack(this.blendingData)
         );
      } else {
         ProtoChunkTicks<Block> protochunkticklist = ProtoChunkTicks.load(this.packedTicks.blocks());
         ProtoChunkTicks<Fluid> protochunkticklist1 = ProtoChunkTicks.load(this.packedTicks.fluids());
         ProtoChunk protochunk = new ProtoChunk(
            chunkcoordintpair,
            this.upgradeData,
            achunksection,
            protochunkticklist,
            protochunkticklist1,
            worldserver,
            iregistry,
            BlendingData.unpack(this.blendingData)
         );
         object = protochunk;
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
         ((ChunkAccess)object).persistentDataContainer.putAll((CompoundTag)this.persistentDataContainer);
      }

      ((ChunkAccess)object).setLightCorrect(this.lightCorrect);
      EnumSet<Types> enumset = EnumSet.noneOf(Types.class);

      for(Types heightmap_type : ((ChunkAccess)object).getPersistedStatus().heightmapsAfter()) {
         long[] along = (long[])this.heightmaps.get(heightmap_type);
         if (along != null) {
            ((ChunkAccess)object).setHeightmap(heightmap_type, along);
         } else {
            enumset.add(heightmap_type);
         }
      }

      Heightmap.primeHeightmaps((ChunkAccess)object, enumset);
      ((ChunkAccess)object)
         .setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(worldserver), this.structureData, worldserver.getSeed()));
      ((ChunkAccess)object).setAllReferences(unpackStructureReferences(worldserver.registryAccess(), chunkcoordintpair, this.structureData));

      for(int j = 0; j < this.postProcessingSections.length; ++j) {
         ((ChunkAccess)object).addPackedPostProcess(this.postProcessingSections[j], j);
      }

      if (chunktype == ChunkType.LEVELCHUNK) {
         return new ImposterProtoChunk((LevelChunk)object, false);
      } else {
         ProtoChunk protochunk1 = (ProtoChunk)object;

         for(CompoundTag nbttagcompound : this.entities) {
            protochunk1.addEntity(nbttagcompound);
         }

         for(CompoundTag nbttagcompound : this.blockEntities) {
            protochunk1.setBlockEntityNbt(nbttagcompound);
         }

         if (this.carvingMask != null) {
            protochunk1.setCarvingMask(new CarvingMask(this.carvingMask, ((ChunkAccess)object).getMinY()));
         }

         return protochunk1;
      }
   }

   private static void logErrors(ChunkPos chunkcoordintpair, int i, String s) {
      LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", new Object[]{chunkcoordintpair.x, i, chunkcoordintpair.z, s});
   }

   private static Codec<PalettedContainerRO<Holder<Biome>>> makeBiomeCodec(Registry<Biome> iregistry) {
      return PalettedContainer.codecRO(iregistry.asHolderIdMap(), iregistry.holderByNameCodec(), Strategy.SECTION_BIOMES, iregistry.getOrThrow(Biomes.PLAINS));
   }

   private static Codec<PalettedContainer<Holder<Biome>>> makeBiomeCodecRW(Registry<Biome> iregistry) {
      return PalettedContainer.codecRW(iregistry.asHolderIdMap(), iregistry.holderByNameCodec(), Strategy.SECTION_BIOMES, iregistry.getOrThrow(Biomes.PLAINS));
   }

   public static ProtoChunkData1_21_R2 copyOf(ServerLevel worldserver, ChunkAccess ichunkaccess) {
      if (!ichunkaccess.canBeSerialized()) {
         throw new IllegalArgumentException("Chunk can't be serialized: " + ichunkaccess);
      } else {
         ChunkPos chunkcoordintpair = ichunkaccess.getPos();
         List<ProtoChunkData1_21_R2.SectionData> list = new ArrayList();
         LevelChunkSection[] achunksection = ichunkaccess.getSections();
         ThreadedLevelLightEngine lightenginethreaded = worldserver.getChunkSource().getLightEngine();

         for(int i = lightenginethreaded.getMinLightSection(); i < lightenginethreaded.getMaxLightSection(); ++i) {
            int j = ichunkaccess.getSectionIndexFromSectionY(i);
            boolean flag = j >= 0 && j < achunksection.length;
            DataLayer nibblearray = lightenginethreaded.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(chunkcoordintpair, i));
            DataLayer nibblearray1 = lightenginethreaded.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(chunkcoordintpair, i));
            DataLayer nibblearray2 = nibblearray != null && !nibblearray.isEmpty() ? nibblearray.copy() : null;
            DataLayer nibblearray3 = nibblearray1 != null && !nibblearray1.isEmpty() ? nibblearray1.copy() : null;
            if (flag || nibblearray2 != null || nibblearray3 != null) {
               LevelChunkSection chunksection = flag ? achunksection[j].copy() : null;
               list.add(new ProtoChunkData1_21_R2.SectionData(i, chunksection, nibblearray2, nibblearray3));
            }
         }

         List<CompoundTag> list1 = new ArrayList(ichunkaccess.getBlockEntitiesPos().size());

         for(BlockPos blockposition : ichunkaccess.getBlockEntitiesPos()) {
            CompoundTag nbttagcompound = ichunkaccess.getBlockEntityNbtForSaving(blockposition, worldserver.registryAccess());
            if (nbttagcompound != null) {
               list1.add(nbttagcompound);
            }
         }

         List<CompoundTag> list2 = new ArrayList();
         long[] along = null;
         if (ichunkaccess.getPersistedStatus().getChunkType() == ChunkType.PROTOCHUNK) {
            ProtoChunk protochunk = (ProtoChunk)ichunkaccess;
            list2.addAll(protochunk.getEntities());
            CarvingMask carvingmask = protochunk.getCarvingMask();
            if (carvingmask != null) {
               along = carvingmask.toArray();
            }
         }

         Map<Types, long[]> map = new EnumMap(Types.class);

         for(Entry<Types, Heightmap> entry : ichunkaccess.getHeightmaps()) {
            if (ichunkaccess.getPersistedStatus().heightmapsAfter().contains(entry.getKey())) {
               long[] along1 = ((Heightmap)entry.getValue()).getRawData();
               map.put((Types)entry.getKey(), (long[])along1.clone());
            }
         }

         PackedTicks ichunkaccess_a = ichunkaccess.getTicksForSerialization(worldserver.getGameTime());
         ShortList[] ashortlist = (ShortList[])Arrays.stream(ichunkaccess.getPostProcessing())
            .map(shortlist -> shortlist != null ? new ShortArrayList(shortlist) : null)
            .toArray(k -> new ShortList[k]);
         CompoundTag nbttagcompound1 = packStructureData(
            StructurePieceSerializationContext.fromLevel(worldserver), chunkcoordintpair, ichunkaccess.getAllStarts(), ichunkaccess.getAllReferences()
         );
         CompoundTag persistentDataContainer = null;
         if (!ichunkaccess.persistentDataContainer.isEmpty()) {
            persistentDataContainer = ichunkaccess.persistentDataContainer.toTagCompound();
         }

         return new ProtoChunkData1_21_R2(
            worldserver.registryAccess().lookupOrThrow(Registries.BIOME),
            chunkcoordintpair,
            ichunkaccess.getMinSectionY(),
            worldserver.getGameTime(),
            ichunkaccess.getInhabitedTime(),
            ichunkaccess.getPersistedStatus(),
            (Packed)Optionull.map(ichunkaccess.getBlendingData(), BlendingData::pack),
            ichunkaccess.getBelowZeroRetrogen(),
            ichunkaccess.getUpgradeData().copy(),
            along,
            map,
            ichunkaccess_a,
            ashortlist,
            ichunkaccess.isLightCorrect(),
            list,
            list2,
            list1,
            nbttagcompound1,
            persistentDataContainer
         );
      }
   }

   public CompoundTag write() {
      CompoundTag nbttagcompound = NbtUtils.addCurrentDataVersion(new CompoundTag());
      nbttagcompound.putInt("xPos", this.chunkPos.x);
      nbttagcompound.putInt("yPos", this.minSectionY);
      nbttagcompound.putInt("zPos", this.chunkPos.z);
      nbttagcompound.putLong("LastUpdate", this.lastUpdateTime);
      nbttagcompound.putLong("InhabitedTime", this.inhabitedTime);
      nbttagcompound.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(this.chunkStatus).toString());
      if (this.blendingData != null) {
         DataResult<Tag> dataresult = Packed.CODEC.encodeStart(NbtOps.INSTANCE, this.blendingData);
         Logger logger = LOGGER;
         dataresult.resultOrPartial(logger::error).ifPresent(nbtbase -> nbttagcompound.put("blending_data", nbtbase));
      }

      if (this.belowZeroRetrogen != null) {
         DataResult<Tag> dataresult = BelowZeroRetrogen.CODEC.encodeStart(NbtOps.INSTANCE, this.belowZeroRetrogen);
         Logger logger = LOGGER;
         dataresult.resultOrPartial(logger::error).ifPresent(nbtbase -> nbttagcompound.put("below_zero_retrogen", nbtbase));
      }

      if (!this.upgradeData.isEmpty()) {
         nbttagcompound.put("UpgradeData", this.upgradeData.write());
      }

      ListTag nbttaglist = new ListTag();
      Codec<PalettedContainerRO<Holder<Biome>>> codec = makeBiomeCodec(this.biomeRegistry);

      for(ProtoChunkData1_21_R2.SectionData ProtoChunkData_b : this.sectionData) {
         CompoundTag nbttagcompound1 = new CompoundTag();
         LevelChunkSection chunksection = ProtoChunkData_b.chunkSection;
         if (chunksection != null) {
            nbttagcompound1.put("block_states", (Tag)BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, chunksection.getStates()).getOrThrow());
            nbttagcompound1.put("biomes", (Tag)codec.encodeStart(NbtOps.INSTANCE, chunksection.getBiomes()).getOrThrow());
         }

         if (ProtoChunkData_b.blockLight != null) {
            nbttagcompound1.putByteArray("BlockLight", ProtoChunkData_b.blockLight.getData());
         }

         if (ProtoChunkData_b.skyLight != null) {
            nbttagcompound1.putByteArray("SkyLight", ProtoChunkData_b.skyLight.getData());
         }

         if (!nbttagcompound1.isEmpty()) {
            nbttagcompound1.putByte("Y", (byte)ProtoChunkData_b.y);
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
      this.heightmaps.forEach((heightmap_type, along) -> nbttagcompound2.put(heightmap_type.getSerializationKey(), new LongArrayTag(along)));
      nbttagcompound.put("Heightmaps", nbttagcompound2);
      nbttagcompound.put("structures", this.structureData);
      if (this.persistentDataContainer != null) {
         nbttagcompound.put("ChunkBukkitValues", this.persistentDataContainer);
      }

      return nbttagcompound;
   }

   private static void saveTicks(CompoundTag nbttagcompound, PackedTicks ichunkaccess_a) {
      ListTag nbttaglist = new ListTag();

      for(SavedTick<Block> ticklistchunk : ichunkaccess_a.blocks()) {
         nbttaglist.add(ticklistchunk.save(block -> BuiltInRegistries.BLOCK.getKey(block).toString()));
      }

      nbttagcompound.put("block_ticks", nbttaglist);
      ListTag nbttaglist1 = new ListTag();

      for(SavedTick<Fluid> ticklistchunk1 : ichunkaccess_a.fluids()) {
         nbttaglist1.add(ticklistchunk1.save(fluidtype -> BuiltInRegistries.FLUID.getKey(fluidtype).toString()));
      }

      nbttagcompound.put("fluid_ticks", nbttaglist1);
   }

   public static ChunkType getChunkTypeFromTag(@Nullable CompoundTag nbttagcompound) {
      return nbttagcompound != null ? ChunkStatus.byName(nbttagcompound.getString("Status")).getChunkType() : ChunkType.PROTOCHUNK;
   }

   @Nullable
   private static PostLoadProcessor postLoadChunk(ServerLevel worldserver, List<CompoundTag> list, List<CompoundTag> list1) {
      return list.isEmpty() && list1.isEmpty() ? null : chunk -> {
         worldserver.timings.syncChunkLoadEntitiesTimer.startTiming();
         if (!list.isEmpty()) {
            worldserver.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(list, worldserver, EntitySpawnReason.LOAD));
         }

         worldserver.timings.syncChunkLoadEntitiesTimer.stopTiming();
         Iterator iterator = list1.iterator();
         worldserver.timings.syncChunkLoadTileEntitiesTimer.startTiming();

         while(iterator.hasNext()) {
            CompoundTag nbttagcompound = (CompoundTag)iterator.next();
            boolean flag = nbttagcompound.getBoolean("keepPacked");
            if (flag) {
               chunk.setBlockEntityNbt(nbttagcompound);
            } else {
               BlockPos blockposition = BlockEntity.getPosFromTag(nbttagcompound);
               BlockEntity tileentity = BlockEntity.loadStatic(blockposition, chunk.getBlockState(blockposition), nbttagcompound, worldserver.registryAccess());
               if (tileentity != null) {
                  chunk.setBlockEntity(tileentity);
               }
            }
         }

         worldserver.timings.syncChunkLoadTileEntitiesTimer.stopTiming();
      };
   }

   private static CompoundTag packStructureData(
      StructurePieceSerializationContext structurepieceserializationcontext,
      ChunkPos chunkcoordintpair,
      Map<Structure, StructureStart> map,
      Map<Structure, LongSet> map1
   ) {
      CompoundTag nbttagcompound = new CompoundTag();
      CompoundTag nbttagcompound1 = new CompoundTag();
      Registry<Structure> iregistry = structurepieceserializationcontext.registryAccess().lookupOrThrow(Registries.STRUCTURE);

      for(Entry<Structure, StructureStart> entry : map.entrySet()) {
         ResourceLocation minecraftkey = iregistry.getKey((Structure)entry.getKey());
         nbttagcompound1.put(minecraftkey.toString(), ((StructureStart)entry.getValue()).createTag(structurepieceserializationcontext, chunkcoordintpair));
      }

      nbttagcompound.put("starts", nbttagcompound1);
      CompoundTag nbttagcompound2 = new CompoundTag();

      for(Entry<Structure, LongSet> entry1 : map1.entrySet()) {
         if (!((LongSet)entry1.getValue()).isEmpty()) {
            ResourceLocation minecraftkey1 = iregistry.getKey((Structure)entry1.getKey());
            nbttagcompound2.put(minecraftkey1.toString(), new LongArrayTag((LongSet)entry1.getValue()));
         }
      }

      nbttagcompound.put("References", nbttagcompound2);
      return nbttagcompound;
   }

   private static Map<Structure, StructureStart> unpackStructureStart(
      StructurePieceSerializationContext structurepieceserializationcontext, CompoundTag nbttagcompound, long i
   ) {
      Map<Structure, StructureStart> map = Maps.newHashMap();
      Registry<Structure> iregistry = structurepieceserializationcontext.registryAccess().lookupOrThrow(Registries.STRUCTURE);
      CompoundTag nbttagcompound1 = nbttagcompound.getCompound("starts");

      for(String s : nbttagcompound1.getAllKeys()) {
         ResourceLocation minecraftkey = ResourceLocation.tryParse(s);
         Structure structure = (Structure)iregistry.getValue(minecraftkey);
         if (structure == null) {
            LOGGER.error("Unknown structure start: {}", minecraftkey);
         } else {
            StructureStart structurestart = StructureStart.loadStaticStart(structurepieceserializationcontext, nbttagcompound1.getCompound(s), i);
            if (structurestart != null) {
               Tag persistentBase = nbttagcompound1.getCompound(s).get("StructureBukkitValues");
               if (persistentBase instanceof CompoundTag) {
                  structurestart.persistentDataContainer.putAll((CompoundTag)persistentBase);
               }

               map.put(structure, structurestart);
            }
         }
      }

      return map;
   }

   private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess iregistrycustom, ChunkPos chunkcoordintpair, CompoundTag nbttagcompound) {
      Map<Structure, LongSet> map = Maps.newHashMap();
      Registry<Structure> iregistry = iregistrycustom.lookupOrThrow(Registries.STRUCTURE);
      CompoundTag nbttagcompound1 = nbttagcompound.getCompound("References");

      for(String s : nbttagcompound1.getAllKeys()) {
         ResourceLocation minecraftkey = ResourceLocation.tryParse(s);
         Structure structure = (Structure)iregistry.getValue(minecraftkey);
         if (structure == null) {
            LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", minecraftkey, chunkcoordintpair);
         } else {
            long[] along = nbttagcompound1.getLongArray(s);
            if (along.length != 0) {
               map.put(
                  structure,
                  new LongOpenHashSet(
                     Arrays.stream(along)
                        .filter(
                           i -> {
                              ChunkPos chunkcoordintpair1 = new ChunkPos(i);
                              if (chunkcoordintpair1.getChessboardDistance(chunkcoordintpair) > 8) {
                                 LOGGER.warn(
                                    "Found invalid structure reference [ {} @ {} ] for chunk {}.",
                                    new Object[]{minecraftkey, chunkcoordintpair1, chunkcoordintpair}
                                 );
                                 return false;
                              } else {
                                 return true;
                              }
                           }
                        )
                        .toArray()
                  )
               );
            }
         }
      }

      return map;
   }

   private static ListTag packOffsets(ShortList[] ashortlist) {
      ListTag nbttaglist = new ListTag();

      for(ShortList shortlist : ashortlist) {
         ListTag nbttaglist1 = new ListTag();
         if (shortlist != null) {
            for(int k = 0; k < shortlist.size(); ++k) {
               nbttaglist1.add(ShortTag.valueOf(shortlist.getShort(k)));
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

   public static record SectionData(int y, @Nullable LevelChunkSection chunkSection, @Nullable DataLayer blockLight, @Nullable DataLayer skyLight) {
   }
}