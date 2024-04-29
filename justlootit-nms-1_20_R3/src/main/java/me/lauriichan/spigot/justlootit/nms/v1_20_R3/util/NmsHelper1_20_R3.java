package me.lauriichan.spigot.justlootit.nms.v1_20_R3.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R3.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.persistence.PersistentDataType;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class NmsHelper1_20_R3 {

    private static final MethodHandle GET_TILE_ENTITY = Access.getTileEntity();
    private static final MethodHandle WRAP = Access.wrap();
    private static final VarHandle DATA_TYPE_REGISTRY = Access.dataTypeRegistry();
    private static final VarHandle TAGS = Access.tags();
    
    private static volatile boolean wrapRequiresClass = true;
    private static volatile boolean dataTypeRegistrySetup = false;

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

        static MethodHandle wrap() {
            Method method = ClassUtil.getMethod(CraftPersistentDataTypeRegistry.class, "wrap", Class.class, Object.class);
            if (method == null) {
                method = ClassUtil.getMethod(CraftPersistentDataTypeRegistry.class, "wrap", PersistentDataType.class, Object.class);
                wrapRequiresClass = false;
                if (method == null) {
                    throw new IllegalStateException("Couldn't find method 'wrap', JustLootIt won't work here.");
                }
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

        static VarHandle tags() {
            Field field = ClassUtil.getField(CompoundTag.class, false, Map.class);
            if (field == null) {
                throw new IllegalStateException("Couldn't find field 'tags', JustLootIt won't be able to convert anything here.");
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
        CraftPersistentDataTypeRegistry registry = (CraftPersistentDataTypeRegistry) DATA_TYPE_REGISTRY.get();
        if (!dataTypeRegistrySetup) {
            dataTypeRegistrySetup = true;
            setupRegistry(registry);
        }
        return registry;
    }
    
    private static void setupRegistry(CraftPersistentDataTypeRegistry registry) {
        wrap(registry, PersistentDataType.BYTE, Byte.valueOf((byte) 0));
        wrap(registry, PersistentDataType.SHORT, Short.valueOf((short) 0));
        wrap(registry, PersistentDataType.INTEGER, Integer.valueOf(0));
        wrap(registry, PersistentDataType.LONG, Long.valueOf(0L));
        wrap(registry, PersistentDataType.FLOAT, Float.valueOf(0f));
        wrap(registry, PersistentDataType.DOUBLE, Double.valueOf(0d));
        wrap(registry, PersistentDataType.BYTE_ARRAY, new byte[0]);
        wrap(registry, PersistentDataType.INTEGER_ARRAY, new int[0]);
        wrap(registry, PersistentDataType.LONG_ARRAY, new long[0]);
        wrap(registry, PersistentDataType.TAG_CONTAINER, new CraftPersistentDataContainer(registry));
    }
    
    private static <P> void wrap(CraftPersistentDataTypeRegistry registry, PersistentDataType<P, ?> type, P value) {
        try {
            WRAP.invokeWithArguments(registry, wrapRequiresClass ? type.getPrimitiveType() : type, value);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to wrap primitive '" + type.getPrimitiveType().getName() + "'", e);
        }
    }
    
    public static void clearCompound(CompoundTag tag) {
        ((Map<?, ?>) TAGS.get(tag)).clear();
    }
    
    public static MinecraftServer getServer() {
        return ((CraftServer) Bukkit.getServer()).getServer();
    }

}
