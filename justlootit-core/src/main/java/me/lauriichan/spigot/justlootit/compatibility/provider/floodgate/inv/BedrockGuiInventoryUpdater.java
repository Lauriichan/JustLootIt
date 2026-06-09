package me.lauriichan.spigot.justlootit.compatibility.provider.floodgate.inv;

import org.bukkit.event.inventory.InventoryType;

import me.lauriichan.minecraft.pluginbase.inventory.ChestSize;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventoryUpdater;

public final class BedrockGuiInventoryUpdater implements IGuiInventoryUpdater {

    private final BedrockGuiInventory inventory;

    private ChestSize size;
    private InventoryType type;

    private String title;

    public BedrockGuiInventoryUpdater(final BedrockGuiInventory inventory) {
        this.inventory = inventory;
        this.size = inventory.getChestSize();
        this.type = inventory.getType();
        this.title = inventory.getTitle();
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public BedrockGuiInventoryUpdater title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public ChestSize chestSize() {
        return size;
    }

    @Override
    public BedrockGuiInventoryUpdater chestSize(ChestSize size) {
        this.size = size;
        this.type = null;
        return this;
    }

    @Override
    public InventoryType type() {
        return type;
    }

    @Override
    public BedrockGuiInventoryUpdater type(InventoryType type) {
        this.size = null;
        this.type = type;
        return this;
    }

    @Override
    public boolean apply() {
        return inventory.apply(this);
    }

}
