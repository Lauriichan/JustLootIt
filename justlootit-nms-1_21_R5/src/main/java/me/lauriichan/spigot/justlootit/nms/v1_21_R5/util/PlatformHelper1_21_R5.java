package me.lauriichan.spigot.justlootit.nms.v1_21_R5.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import org.bukkit.Keyed;
import org.bukkit.craftbukkit.v1_21_R5.CraftRegistry;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaLookup;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

public final class PlatformHelper1_21_R5 {

    private static final MethodHandle GET_ENTITY_LOOKUP = Access.getEntityLookup();
    private static final MethodHandle BUKKIT_TO_MINECRAFT_HOLDER = Access.bukkitToMinecraftHolder();

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
        
        static MethodHandle bukkitToMinecraftHolder() {
            Method method = ClassUtil.getMethod(CraftRegistry.class, "bukkitToMinecraftHolder", Keyed.class);
            if (method == null || !Holder.class.isAssignableFrom(method.getReturnType())) {
                return null;
            }
            return JavaLookup.PLATFORM.unreflect(method);
        }

    }

    private PlatformHelper1_21_R5() {
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

}
