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
    public boolean onEventClose(final HumanEntity entity, final IGuiInventory inventory, final VersionHandler versionHandler) {
        final Long id = inventory.attrUnset(ATTR_ID, Long.class);
        if (id == null) {
            return false;
        }
        final PlayerAdapter player = versionHandler.getPlayer(entity.getUniqueId());
        if (player == null) {
            return false;
        }
        player.getCapability(StorageCapability.class).ifPresent(capability -> {
            System.out.println("SAVING: " + id);
            try {
                capability.storage().write(new CachedInventory(id, inventory.getInventory()));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        return false;
    }

    @Override
    public boolean onEventClick(final HumanEntity entity, final IGuiInventory inventory, final VersionHandler versionHandler,
        final InventoryClickEvent event) {
        return false;
    }

}
