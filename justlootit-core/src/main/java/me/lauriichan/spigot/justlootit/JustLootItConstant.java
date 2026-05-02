package me.lauriichan.spigot.justlootit;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.event.inventory.InventoryType;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaLookup;

public final class JustLootItConstant {

    public static record MaterialInventory(Material material, InventoryType inventoryType) {}

    public static final String PLUGIN_NAMESPACE = "justlootit";

    public static final String STATIC_CONTAINER_REFRESH_KEY = "container/static";
    public static final String FRAME_CONTAINER_REFRESH_KEY_FORMAT = "container/frame/%s/%s";
    public static final String COMPATIBILITY_CONTAINER_REFRESH_KEY_FORMAT = "container/compatibility/%s/%s";

    public static final ObjectList<InventoryType> UNSUPPORTED_CONTAINER_TYPES;
    public static final ObjectList<MaterialInventory> SUPPORTED_CONTAINER_ITEMS;

    private JustLootItConstant() {
        throw new UnsupportedOperationException();
    }

    static {
        // UNSUPPORTED_CONTAINER_TYPES
        Class<?> CraftInventoryCreator_class = ClassUtil
            .findClass(JustLootItPlugin.get().platform().version().craftClassPath("inventory.util.CraftInventoryCreator"));
        MethodHandle CraftInventoryCreator_INSTANCE = JavaLookup.PLATFORM
            .unreflectGetter(ClassUtil.getField(CraftInventoryCreator_class, "INSTANCE"));
        Field defaultConverterField = ClassUtil.getField(CraftInventoryCreator_class, "DEFAULT_CONVERTER");
        if (defaultConverterField == null) {
            defaultConverterField = ClassUtil.getField(CraftInventoryCreator_class, "defaultConverter");
        }
        MethodHandle CraftInventoryCreator_DEFAULT_CONVERTER = JavaLookup.PLATFORM.unreflectGetter(defaultConverterField);
        MethodHandle CraftInventoryCreator_converterMap = JavaLookup.PLATFORM
            .unreflectGetter(ClassUtil.getField(CraftInventoryCreator_class, "converterMap"));

        ObjectList<InventoryType> unsupportedContainerTypes;
        try {
            Object instance = CraftInventoryCreator_INSTANCE.invoke();
            Object defaultConverter = CraftInventoryCreator_DEFAULT_CONVERTER.invoke(instance);
            Map<InventoryType, ?> map = (Map<InventoryType, ?>) CraftInventoryCreator_converterMap.invoke(instance);
            unsupportedContainerTypes = new ObjectArrayList<>();
            for (Map.Entry<InventoryType, ?> entry : map.entrySet()) {
                if (defaultConverter != entry.getValue()) {
                    unsupportedContainerTypes.add(entry.getKey());
                }
            }
            unsupportedContainerTypes = ObjectLists.unmodifiable(unsupportedContainerTypes);
        } catch (Throwable throwable) {
            unsupportedContainerTypes = ObjectLists.emptyList();
        }
        UNSUPPORTED_CONTAINER_TYPES = unsupportedContainerTypes;

        {
            ObjectArrayList<MaterialInventory> list = new ObjectArrayList<>();
            for (InventoryType type : InventoryType.values()) {
                if (UNSUPPORTED_CONTAINER_TYPES.contains(type)) {
                    continue;
                }
                Material material = Material.getMaterial(type.name());
                if (material == null) {
                    continue;
                }
                BlockState state = Bukkit.createBlockData(material).createBlockState().copy();
                if (state instanceof Container) {
                    list.add(new MaterialInventory(material, type));
                }
            }
            SUPPORTED_CONTAINER_ITEMS = ObjectLists.unmodifiable(list);
        }
    }

}
