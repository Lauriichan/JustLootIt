package me.lauriichan.spigot.justlootit.inventory.handler.loot;

import org.bukkit.entity.HumanEntity;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventoryUpdater;
import me.lauriichan.minecraft.pluginbase.inventory.paged.PagedInventoryHandler;
import me.lauriichan.spigot.justlootit.data.CachedInventory;
import me.lauriichan.spigot.justlootit.message.UIInventoryNames;

public final class CachedLootUIHandler extends BaseLootUIHandler {

    public static final CachedLootUIHandler LOOT_HANDLER = new CachedLootUIHandler();

    public static final String ATTR_CACHED_INVENTORY = "CachedInventory";

    private CachedLootUIHandler() {}

    @Override
    public void onInit(HumanEntity entity, IGuiInventory inventory) {
        if (!inventory.attrHas(ATTR_CACHED_INVENTORY)) {
            return;
        }
        final Actor<HumanEntity> actor = plugin.actor(entity);
        final CachedInventory cachedInventory = inventory.attr(ATTR_CACHED_INVENTORY, CachedInventory.class);
        final int columnAmount = IGuiInventory.getColumnAmount(cachedInventory.getType());
        final int rowAmount = cachedInventory.size() / columnAmount;

        IGuiInventoryUpdater updater = inventory.updater().title(actor.getTranslatedMessageAsString(UIInventoryNames.LOOT_UI_NAME));
        if (columnAmount == 9) {
            updater.chestSize(CHEST_VALUES[rowAmount - 1]);
        } else {
            updater.type(cachedInventory.getType());
        }
        inventory.attrSet(PagedInventoryHandler.PLAYER_PROPERTY, entity);
        if (!updater.apply()) {
            inventory.update();
        }
    }

    @Override
    public void onUpdate(IGuiInventory inventory, boolean changed) {
        if (!inventory.attrHas(ATTR_CACHED_INVENTORY)) {
            return;
        }
        inventory.getInventory().setContents(inventory.attrUnset(ATTR_CACHED_INVENTORY, CachedInventory.class).getItems());
    }

}
