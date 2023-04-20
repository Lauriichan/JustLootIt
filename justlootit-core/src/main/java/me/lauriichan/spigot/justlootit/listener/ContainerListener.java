package me.lauriichan.spigot.justlootit.listener;

import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.capability.PlayerGUICapability;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.data.*;
import me.lauriichan.spigot.justlootit.data.CacheLookupTable.WorldEntry;
import me.lauriichan.spigot.justlootit.inventory.ChestSize;
import me.lauriichan.spigot.justlootit.inventory.IGuiInventory;
import me.lauriichan.spigot.justlootit.inventory.handler.loot.LootUIHandler;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.storage.IStorage;
import me.lauriichan.spigot.justlootit.storage.Storable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.util.UUID;

public class ContainerListener implements Listener {

    private final VersionHandler versionHandler;

    public ContainerListener(final VersionHandler versionHandler) {
        this.versionHandler = versionHandler;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        BlockState state = block.getState();
        if (!(state instanceof org.bukkit.block.Container)) {
            return;
        }
        org.bukkit.block.Container container = (org.bukkit.block.Container) state;
        PersistentDataContainer dataContainer = container.getPersistentDataContainer();
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        accessContainer(block.getLocation(), dataContainer, event, event.getPlayer(),
            dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG).longValue());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        EntityType type = entity.getType();
        if (type != EntityType.MINECART_CHEST && type != EntityType.MINECART_HOPPER && type != EntityType.CHEST_BOAT) {
            return;
        }
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        if (!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        accessContainer(entity.getLocation(), dataContainer, event, event.getPlayer(),
            dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG).longValue());
    }

    private void accessContainer(Location location, PersistentDataContainer data, Cancellable event, Player bukkitPlayer, long id) {
        WorldEntry entryId = new WorldEntry(location.getWorld(), id);
        PlayerAdapter player = versionHandler.getPlayer(bukkitPlayer);
        UUID playerId = bukkitPlayer.getUniqueId();
        versionHandler.getLevel(location.getWorld()).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
            Container dataContainer = (Container) capability.storage().read(id);
            if (dataContainer == null) {
                data.remove(JustLootItKey.identity());
                return;
            }
            event.setCancelled(true);
            player.getCapability(StorageCapability.class).ifPresent(playerCapability -> {
                IStorage<Storable> playerStorage = playerCapability.storage();
                CacheLookupTable lookupTable = CacheLookupTable.retrieve(playerStorage);
                if (!dataContainer.access(playerId)) {
                    if (lookupTable.access(entryId)) {
                        CachedInventory cachedInventory = (CachedInventory) playerStorage.read(lookupTable.getEntryIdByMapped(entryId));
                        int rowSize = IGuiInventory.getRowSize(cachedInventory.getType());
                        if (cachedInventory.size() % rowSize == 0) {
                            int columnAmount = cachedInventory.size() / rowSize;
                            if (!(rowSize == 9 && (columnAmount > 6 || columnAmount < 1))) {
                                player.getCapability(PlayerGUICapability.class).ifPresent(guiCapability -> {
                                    IGuiInventory inventory = guiCapability.gui();
                                    if (rowSize == 9) {
                                        inventory.setChestSize(ChestSize.values()[columnAmount - 1]);
                                    } else {
                                        inventory.setType(cachedInventory.getType());
                                    }
                                    inventory.getInventory().setContents(cachedInventory.getItems());
                                    inventory.attrSet(LootUIHandler.ATTR_ID, cachedInventory.id());
                                    inventory.setHandler(LootUIHandler.LOOT_HANDLER);
                                    inventory.open(bukkitPlayer);
                                });
                                return;
                            }
                        }
                        playerStorage.delete(cachedInventory.id());
                    }

                    Duration duration = dataContainer.durationUntilNextAccess(playerId);
                    if (duration.isNegative()) {
                        // TODO: Send message, can never be accessed again
                        return;
                    }
                    // TODO: Send message, not accessible yet
                    return;
                }

                player.getCapability(PlayerGUICapability.class).ifPresent(guiCapability -> {
                    IGuiInventory inventory = guiCapability.gui();
                    if (dataContainer instanceof IInventoryContainer container) {
                        container.fill(player, location, inventory.getInventory());
                    } else {
                        // Do nothing, no need to allocate anything if we have no inventory
                        return;
                    }
                    inventory.attrSet(LootUIHandler.ATTR_ID, lookupTable.acquire(entryId));
                    inventory.setHandler(LootUIHandler.LOOT_HANDLER);
                    inventory.open(bukkitPlayer);
                });
            });
        }, () -> event.setCancelled(true));
    }

}
