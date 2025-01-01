package me.lauriichan.spigot.justlootit.nms.v1_21_R3.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.craftbukkit.v1_21_R3.block.data.CraftBlockData;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.laylib.reflection.JavaLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

public final class PlatformHelper1_21_R3 {

    private static final MethodHandle GET_ENTITY_LOOKUP = Access.getEntityLookup();
    private static final Field ENUM_VALUES = Access.enumValues();

    private static volatile boolean blockDataPatchApplied = false;

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

        static Field enumValues() {
            Field field = ClassUtil.getField(CraftBlockData.class, "ENUM_VALUES");
            if (field == null || !Map.class.isAssignableFrom(field.getType())) {
                throw new IllegalStateException("Couldn't find field 'ENUM_VALUES', JustLootIt won't be able to convert anything here.");
            }
            return field;
        }

    }

    private PlatformHelper1_21_R3() {
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

    @SuppressWarnings("rawtypes")
    public static void patchCraftBlockDataEnumValues() {
        if (blockDataPatchApplied) {
            return;
        }
        blockDataPatchApplied = true;
        if (JavaAccess.PLATFORM.getValue(ENUM_VALUES) instanceof ConcurrentHashMap) {
            return;
        }
        JavaAccess.PLATFORM.setValue(ENUM_VALUES, new ConcurrentHashMap());
    }

}
