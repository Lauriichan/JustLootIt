package me.lauriichan.spigot.justlootit.nms.v1_20_R2.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v1_20_R2.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class NmsHelper1_20_R2 {

    private static final MethodHandle GET_TILE_ENTITY = Access.getTileEntity();
    private static final VarHandle DATA_TYPE_REGISTRY = Access.dataTypeRegistry();
    private static final VarHandle TAGS = Access.tags();
    
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
        // Initialize registry with primitives to prevent concurrent modification exceptions
        registry.wrap(Byte.class, (byte) 0);
        registry.wrap(Short.class, (short) 0);
        registry.wrap(Integer.class, 0);
        registry.wrap(Long.class, 0L);
        registry.wrap(Float.class, 0f);
        registry.wrap(Double.class, 0d);
        registry.wrap(byte[].class, new byte[0]);
        registry.wrap(int[].class, new int[0]);
        registry.wrap(long[].class, new long[0]);
        registry.wrap(PersistentDataContainer.class, new CraftPersistentDataContainer(registry));
    }
    
    public static void clearCompound(CompoundTag tag) {
        ((Map<?, ?>) TAGS.get(tag)).clear();
    }
    
    public static MinecraftServer getServer() {
        return ((CraftServer) Bukkit.getServer()).getServer();
    }

}
