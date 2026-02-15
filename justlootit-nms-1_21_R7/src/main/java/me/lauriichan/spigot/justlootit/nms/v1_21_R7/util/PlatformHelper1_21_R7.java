package me.lauriichan.spigot.justlootit.nms.v1_21_R7.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Keyed;
import org.bukkit.craftbukkit.v1_21_R7.CraftRegistry;

import com.mojang.serialization.Codec;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;
import net.minecraft.world.level.entity.LevelEntityGetter;

public final class PlatformHelper1_21_R7 {

    private static final MethodHandle GET_ENTITY_LOOKUP = Access.getEntityLookup();
    private static final MethodHandle GET_WORLD_LOADER_CONTEXT = Access.getWorldLoaderContext();
    private static final MethodHandle BUKKIT_TO_MINECRAFT_HOLDER = Access.getBukkitToMinecraftHolder();

    private static final MethodHandle UPGRADE_CHUNK_TAG = Access.getUpgradeChunkTag();
    private static final MethodHandle BIOME_CONTAINER_RW_CODEC = Access.getBiomeContainerRWCodec();

    private static final class Access {

        private Access() {
            throw new UnsupportedOperationException();
        }

        static MethodHandle getEntityLookup() {
            Class<?> chunkSystemLevel = ClassUtil.findClass("ca.spottedleaf.moonrise.patches.chunk_system.level.ChunkSystemLevel");
            if (chunkSystemLevel == null) {
                return null;
            }
            Class<?> entityLookup = ClassUtil.findClass("ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup");
            if (entityLookup == null) {
                return null;
            }
            Method method = ClassUtil.getMethod(chunkSystemLevel, "moonrise$getEntityLookup");
            if (method == null || !entityLookup.isAssignableFrom(method.getReturnType())) {
                throw new IllegalStateException("Couldn't find method 'moonrise$getEntityLookup', JustLootIt won't work here.");
            }
            return JavaLookup.PLATFORM.unreflect(method);
        }

        static MethodHandle getWorldLoaderContext() {
            Field field = ClassUtil.getField(MinecraftServer.class, "worldLoaderContext");
            if (field == null) {
                return null;
            }
            return JavaLookup.PLATFORM.unreflectGetter(field);
        }

        static MethodHandle getBukkitToMinecraftHolder() {
            Method method = ClassUtil.getMethod(CraftRegistry.class, "bukkitToMinecraftHolder", Keyed.class);
            if (method == null || !Holder.class.isAssignableFrom(method.getReturnType())) {
                return null;
            }
            return JavaLookup.PLATFORM.unreflect(method);
        }

        static MethodHandle getUpgradeChunkTag() {
            Method method = ClassUtil.getMethod(SimpleRegionStorage.class, "upgradeChunkTag", CompoundTag.class, int.class,
                CompoundTag.class, LevelAccessor.class);
            if (method == null || !CompoundTag.class.isAssignableFrom(method.getReturnType())) {
                return null;
            }
            return JavaLookup.PLATFORM.unreflect(method);
        }

        static MethodHandle getBiomeContainerRWCodec() {
            Method method = ClassUtil.getMethod(PalettedContainerFactory.class, "biomeContainerRWCodec");
            if (method == null || !Codec.class.isAssignableFrom(method.getReturnType())) {
                return null;
            }
            return JavaLookup.PLATFORM.unreflect(method);
        }

    }

    private PlatformHelper1_21_R7() {
        throw new UnsupportedOperationException();
    }

    public static LevelEntityGetter<Entity> getEntityGetter(ServerLevel level) {
        if (GET_ENTITY_LOOKUP == null) {
            return level.entityManager.getEntityGetter();
        }
        try {
            return (LevelEntityGetter<Entity>) GET_ENTITY_LOOKUP.invoke(level);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to retrieve entity lookup", e);
        }
    }

    public static <B extends Keyed, M> Holder<M> bukkitToMinecraftHolder(final B bukkit, final ResourceKey<Registry<M>> registry) {
        if (BUKKIT_TO_MINECRAFT_HOLDER == null) {
            return CraftRegistry.bukkitToMinecraftHolder(bukkit, registry);
        }
        try {
            return (Holder<M>) BUKKIT_TO_MINECRAFT_HOLDER.invoke(bukkit);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to retrieve holder", e);
        }
    }

    public static WorldLoader.DataLoadContext getLoadContext(MinecraftServer server) {
        // Fix for weird difference between Paper and Spigot mojang mappings
        try {
            return server.worldLoader;
        } catch (NoSuchFieldError err) {
            try {
                return (WorldLoader.DataLoadContext) GET_WORLD_LOADER_CONTEXT.invoke(server);
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to retrieve world loader", e);
            }
        }
    }

    public static CompoundTag upgradeChunkTag(SimpleRegionStorage storage, CompoundTag chunkTag, int fallbackVersion,
        CompoundTag contextTag, ChunkPos pos) {
        try {
            return storage.upgradeChunkTag(chunkTag, fallbackVersion, contextTag, pos, null);
        } catch (NoSuchMethodError err) {
            try {
                return (CompoundTag) UPGRADE_CHUNK_TAG.invoke(storage, chunkTag, fallbackVersion, contextTag, null);
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to upgrade chunk tag", e);
            }
        }
    }

    public static Codec<PalettedContainer<Holder<Biome>>> biomeContainerCodecRW(PalettedContainerFactory containerFactory) {
        try {
            return containerFactory.biomeContainerCodecRW();
        } catch (NoSuchMethodError err) {
            try {
                return (Codec<PalettedContainer<Holder<Biome>>>) BIOME_CONTAINER_RW_CODEC.invoke(containerFactory);
            } catch (Throwable e) {
                throw new IllegalStateException("Failed to retrieve biome container codec", e);
            }
        }
    }
}
