package me.lauriichan.spigot.justlootit.capability;

import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;
import me.lauriichan.minecraft.pluginbase.inventory.GuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.spigot.justlootit.compatibility.provider.floodgate.inv.BedrockGuiInventory;
import me.lauriichan.spigot.justlootit.nms.capability.ICapability;

public final class PlayerGUICapability implements ICapability {

    private final boolean isBedrock;
    private IGuiInventory gui;

    public PlayerGUICapability(boolean isBedrock) {
        this.isBedrock = isBedrock;
    }

    public IGuiInventory gui() {
        if (gui != null) {
            return gui;
        }
        return gui = create();
    }

    private IGuiInventory create() {
        if (isBedrock) {
            return new BedrockGuiInventory("Inventory", ChestSize.GRID_3x9);
        }
        return new GuiInventory("Inventory", ChestSize.GRID_3x9);
    }

    @Override
    public void terminate() {
        if (gui != null) {
            gui.clear();
            gui = null;
        }
    }

}
