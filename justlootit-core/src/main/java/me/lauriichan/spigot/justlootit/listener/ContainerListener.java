package me.lauriichan.spigot.justlootit.listener;

import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.data.CacheLookupTable;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.storage.Storage;

import java.time.Duration;
import java.util.UUID;

import org.bukkit.World;
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
        if(!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        accessContainer(dataContainer, event, event.getPlayer(), block.getWorld(), dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG).longValue());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteractEntity(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        EntityType type = entity.getType();
        if (type != EntityType.MINECART_CHEST && type != EntityType.MINECART_HOPPER && type != EntityType.CHEST_BOAT) {
            return;
        }
        PersistentDataContainer dataContainer = entity.getPersistentDataContainer();
        if(!dataContainer.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        accessContainer(dataContainer, event, event.getPlayer(), entity.getWorld(), dataContainer.get(JustLootItKey.identity(), PersistentDataType.LONG).longValue());
    }

    private void accessContainer(PersistentDataContainer data, Cancellable event, Player bukkitPlayer, World world, long id) {
        PlayerAdapter player = versionHandler.getPlayer(bukkitPlayer);
        UUID playerId = bukkitPlayer.getUniqueId();
        versionHandler.getLevel(world).getCapability(StorageCapability.class).ifPresentOrElse(capability -> {
            Container dataContainer = (Container) capability.storage().read(id);
            if (dataContainer == null) {
                data.remove(JustLootItKey.identity());
                return;
            }
            event.setCancelled(true);
            player.getCapability(StorageCapability.class).ifPresentOrElse(playerCapability -> {
                Storage<Storable> playerStorage = playerCapability.storage();
                CacheLookupTable lookupTable = CacheLookupTable.retrieve(playerStorage);
                if (!dataContainer.access(playerId)) {
                    if (lookupTable.access(id)) {
                        long playerCacheId = lookupTable.getEntryIdByMapped(id);
                        // TODO: Open cached inventory
                        // TODO: Save container after use
                        return;
                    }
                    Duration duration = dataContainer.durationUntilNextAccess(playerId);
                    if (duration.isNegative()) {
                        // TODO: Send message, can never be accessed again
                        return;
                    }
                    // TODO: Send message, not accessible yet
                    return;
                }
                // TODO: Access container
                long playerCacheId = lookupTable.acquire(id);
                // TODO: Save container after use
            }, () -> {
                // TODO: No storage available for some reason?
            });
        }, () -> {
            event.setCancelled(true);
            // TODO: Send message, storage not available for some reason?
        });
    }

}
