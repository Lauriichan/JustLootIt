package me.lauriichan.spigot.justlootit;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.event.inventory.InventoryType;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;

public final class JustLootItConstant {

    public static final List<InventoryType> UNSUPPORTED_CONTAINER_TYPES;

    private JustLootItConstant() {
        throw new UnsupportedOperationException();
    }

    static {
        // UNSUPPORTED_CONTAINER_TYPES
        Class<?> CraftInventoryCreator_class = ClassUtil.findClass(JustLootItPlugin.get().platform().version().craftClassPath("inventory.util.CraftInventoryCreator"));
        MethodHandle CraftInventoryCreator_INSTANCE = JavaAccess
            .accessFieldGetter(ClassUtil.getField(CraftInventoryCreator_class, "INSTANCE"));
        MethodHandle CraftInventoryCreator_DEFAULT_CONVERTER = JavaAccess
            .accessFieldGetter(ClassUtil.getField(CraftInventoryCreator_class, "DEFAULT_CONVERTER"));
        MethodHandle CraftInventoryCreator_converterMap = JavaAccess
            .accessFieldGetter(ClassUtil.getField(CraftInventoryCreator_class, "converterMap"));

        List<InventoryType> unsupportedContainerTypes;
        try {
            Object instance = CraftInventoryCreator_INSTANCE.invoke();
            Object defaultConverter = CraftInventoryCreator_DEFAULT_CONVERTER.invoke(instance);
            Map<InventoryType, ?> map = (Map<InventoryType, ?>) CraftInventoryCreator_converterMap.invoke(instance);
            unsupportedContainerTypes = new ArrayList<>();
            for (Map.Entry<InventoryType, ?> entry : map.entrySet()) {
                if (defaultConverter != entry.getValue()) {
                    unsupportedContainerTypes.add(entry.getKey());
                }
            }
            unsupportedContainerTypes = Collections.unmodifiableList(unsupportedContainerTypes);
        } catch (Throwable throwable) {
            unsupportedContainerTypes = Collections.emptyList();
        }
        UNSUPPORTED_CONTAINER_TYPES = unsupportedContainerTypes;
        // UNSUPPORTED_CONTAINER_TYPES
    }

}
