package me.lauriichan.spigot.justlootit.capability;

import me.lauriichan.spigot.justlootit.inventory.ChestSize;
import me.lauriichan.spigot.justlootit.inventory.GuiInventory;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.capability.ICapability;

public final class PlayerGUICapability implements ICapability {

    private final VersionHandler versionHandler;
    private GuiInventory gui;

    public PlayerGUICapability(final VersionHandler versionHandler) {
        this.versionHandler = versionHandler;
    }

    public GuiInventory gui() {
        if (gui != null) {
            return gui;
        }
        return gui = new GuiInventory(versionHandler, "Inventory", ChestSize.GRID_3x9);
    }

    @Override
    public void terminate() {
        if (gui != null) {
            gui.clear();
            gui = null;
        }
    }

}
