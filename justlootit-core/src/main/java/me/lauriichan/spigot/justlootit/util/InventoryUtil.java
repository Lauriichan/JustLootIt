package me.lauriichan.spigot.justlootit.util;

import org.bukkit.block.DoubleChest;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;

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
        if(inventory.getHolder() instanceof DoubleChest doubleChest) {
            return ChestSize.GRID_6x9.inventorySize();
        }
        return inventory.getSize();
    }

}
