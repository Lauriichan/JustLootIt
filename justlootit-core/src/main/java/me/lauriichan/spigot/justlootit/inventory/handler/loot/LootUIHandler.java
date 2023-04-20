package me.lauriichan.spigot.justlootit.inventory.handler.loot;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;

import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.data.CachedInventory;
import me.lauriichan.spigot.justlootit.inventory.IGuiInventory;
import me.lauriichan.spigot.justlootit.inventory.IHandler;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public final class LootUIHandler implements IHandler {

    public static final LootUIHandler LOOT_HANDLER = new LootUIHandler();
    public static final String ATTR_ID = "PlayerStorageId";

    private LootUIHandler() {}

    @Override
    public boolean onEventClose(HumanEntity entity, IGuiInventory inventory, VersionHandler versionHandler) {
        Long id = inventory.attrUnset(ATTR_ID, Long.class);
        if(id == null) {
            return false;
        }
        PlayerAdapter player = versionHandler.getPlayer(entity.getUniqueId());
        if(player == null) {
            return false;
        }
        player.getCapability(StorageCapability.class).ifPresent(capability -> {
            capability.storage().write(new CachedInventory(id.longValue(), inventory.getInventory()));
        });
        return false;
    }

    @Override
    public boolean onEventClick(HumanEntity entity, IGuiInventory inventory, VersionHandler versionHandler, InventoryClickEvent event) {
        return false;
    }

}
