package me.lauriichan.spigot.justlootit.nms.v1_20_R2.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.persistence.CraftPersistentDataTypeRegistry;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class NmsHelper1_20_R2 {

    private static final MethodHandle GET_TILE_ENTITY = Access.getTileEntity();
    private static final VarHandle DATA_TYPE_REGISTRY = Access.dataTypeRegistry();

    private static final class Access {

        private Access() {
            throw new UnsupportedOperationException();
        }

        static MethodHandle getTileEntity() {
            Method method = ClassUtil.getMethod(CraftBlockEntityState.class, "getTileEntity");
            if (method == null || !BlockEntity.class.isAssignableFrom(method.getReturnType())) {
                throw new IllegalStateException("Couldn't find method 'getTileEntity', JustLootIt won't work here.");
            }
            return JavaAccess.accessMethod(method);
        }

        static VarHandle dataTypeRegistry() {
            Field field = ClassUtil.getField(CraftEntity.class, "DATA_TYPE_REGISTRY");
            if (field == null || !CraftPersistentDataTypeRegistry.class.isAssignableFrom(field.getType())) {
                throw new IllegalStateException("Couldn't find field 'DATA_TYPE_REGISTRY', JustLootIt won't be able to convert anything here.");
            }
            return JavaAccess.accessField(field);
        }

    }
    
    public static <E extends BlockEntity> E getTileEntity(CraftBlockEntityState<E> state) {
        try {
            return (E) GET_TILE_ENTITY.invoke(state);
        } catch (Throwable e) {
            return null;
        }
    }
    
    public static CraftPersistentDataTypeRegistry dataTypeRegistry() {
        return (CraftPersistentDataTypeRegistry) DATA_TYPE_REGISTRY.get();
    }

}
