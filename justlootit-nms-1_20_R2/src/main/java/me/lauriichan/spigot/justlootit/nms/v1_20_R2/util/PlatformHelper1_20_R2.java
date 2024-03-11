package me.lauriichan.spigot.justlootit.nms.v1_20_R2.util;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

public final class PlatformHelper1_20_R2 {

    private static final MethodHandle GET_ENTITY_LOOKUP = Access.getEntityLookup();

    private static final class Access {

        private Access() {
            throw new UnsupportedOperationException();
        }

        static MethodHandle getEntityLookup() {
            Class<?> entityLookup = ClassUtil.findClass("io.papermc.paper.chunk.system.entity.EntityLookup");
            if (entityLookup == null) {
                return null;
            }
            Method method = ClassUtil.getMethod(ServerLevel.class, "getEntityLookup", entityLookup);
            if (method == null) {
                throw new IllegalStateException("Couldn't find method 'getEntityLookup', JustLootIt won't work here.");
            }
            return JavaAccess.accessMethod(method);
        }

    }

    private PlatformHelper1_20_R2() {
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
