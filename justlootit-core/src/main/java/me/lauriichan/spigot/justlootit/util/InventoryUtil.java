package me.lauriichan.spigot.justlootit.util;

import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.type.Chest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;

import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;
import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.JustLootItConstant;
import me.lauriichan.spigot.justlootit.JustLootItFlag;

public final class InventoryUtil {

    private InventoryUtil() {
        throw new UnsupportedOperationException();
    }

    public static ItemStack[] getContents(Inventory inventory) {
        if (inventory.getHolder() instanceof DoubleChest doubleChest) {
            DoubleChestInventory chestInventory = (DoubleChestInventory) doubleChest.getInventory();
            ItemStack[] contents = new ItemStack[ChestSize.GRID_6x9.inventorySize()];
            System.arraycopy(chestInventory.getLeftSide().getContents(), 0, contents, 0, ChestSize.GRID_3x9.inventorySize());
            System.arraycopy(chestInventory.getRightSide().getContents(), 0, contents, ChestSize.GRID_3x9.inventorySize(),
                ChestSize.GRID_3x9.inventorySize());
            return contents;
        }
        return inventory.getContents();
    }

    public static int getSize(Inventory inventory) {
        if (inventory.getHolder() instanceof DoubleChest) {
            return ChestSize.GRID_6x9.inventorySize();
        }
        return inventory.getSize();
    }

    public static boolean isLootContainer(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder();
        if (!(holder instanceof PersistentDataHolder dataHolder)) {
            return false;
        }
        if (!JustLootItFlag.TILE_ENTITY_CONTAINERS.isSet()
            && JustLootItConstant.UNSUPPORTED_CONTAINER_TYPES.contains(inventory.getType())) {
            return false;
        }
        PersistentDataContainer dataContainer = dataHolder.getPersistentDataContainer();
        return JustLootItAccess.hasIdentity(dataContainer) || holder instanceof Chest && JustLootItAccess.hasAnyOffset(dataContainer);
    }

}
