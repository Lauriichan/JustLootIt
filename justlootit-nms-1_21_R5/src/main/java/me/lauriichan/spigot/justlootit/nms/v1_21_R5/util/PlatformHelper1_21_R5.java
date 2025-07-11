package me.lauriichan.spigot.justlootit.nms.v1_21_R5.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

public final class PlatformHelper1_21_R5 {

    private static final MethodHandle GET_ENTITY_LOOKUP = Access.getEntityLookup();

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

}
