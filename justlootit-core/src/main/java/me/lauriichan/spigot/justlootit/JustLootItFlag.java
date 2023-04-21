package me.lauriichan.spigot.justlootit;

import java.util.function.Supplier;

import me.lauriichan.laylib.reflection.ClassUtil;

public final class JustLootItFlag {

    public static final JustLootItFlag TILE_ENTITY_CONTAINERS = newFlag("TileEntityContainers", () -> {
        return ClassUtil.findClass("io.papermc.paper.inventory.PaperInventoryCustomHolderContainer") != null;
    });

    public static JustLootItFlag newFlag(String name, Supplier<Boolean> supplier) {
        return new JustLootItFlag(name, supplier);
    }

    private final String name;
    private final Supplier<Boolean> supplier;
    private volatile boolean set = false;

    private JustLootItFlag(final String name, final Supplier<Boolean> supplier) {
        this.name = name;
        this.supplier = supplier;
    }

    public final String name() {
        return name;
    }

    public final boolean isSet() {
        return set;
    }

    public final JustLootItFlag update() {
        set = supplier.get();
        return this;
    }

}
