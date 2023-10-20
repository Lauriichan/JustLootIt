package me.lauriichan.spigot.justlootit.capability;

import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;
import me.lauriichan.minecraft.pluginbase.inventory.GuiInventory;
import me.lauriichan.spigot.justlootit.nms.capability.ICapability;

public final class PlayerGUICapability implements ICapability {

    private GuiInventory gui;

    public GuiInventory gui() {
        if (gui != null) {
            return gui;
        }
        return gui = new GuiInventory("Inventory", ChestSize.GRID_3x9);
    }

    @Override
    public void terminate() {
        if (gui != null) {
            gui.clear();
            gui = null;
        }
    }

}
