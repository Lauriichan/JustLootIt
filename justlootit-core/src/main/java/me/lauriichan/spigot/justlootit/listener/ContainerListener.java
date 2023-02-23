package me.lauriichan.spigot.justlootit.listener;

import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.util.UUIDTagType;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ContainerListener implements Listener {

    private static final List<EntityType> validEntities = Arrays.asList(EntityType.MINECART_CHEST,
            EntityType.CHEST_BOAT,
            EntityType.MINECART_HOPPER);

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (!(block.getState() instanceof Lootable)) {
            return;
        }

        Container chest = (Container) block.getState();
        UUID container = getContainerUuid(chest.getPersistentDataContainer(), (Lootable) chest);
        if (container != null) {
            // TODO open the custom container
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!validEntities.contains(entity.getType())) {
            return;
        }

        if (!(entity instanceof Lootable)) {
            return;
        }

        UUID container = getContainerUuid(entity.getPersistentDataContainer(), (Lootable) entity);
        if (container != null) {
            // TODO open the custom container
            event.setCancelled(true);
        }
    }

    private UUID getContainerUuid(PersistentDataContainer container, Lootable lootable) {
        if (container.has(JustLootItKey.IDENTITY, UUIDTagType.TYPE)) {
            return container.get(JustLootItKey.IDENTITY, UUIDTagType.TYPE);
        }

        if (lootable.getLootTable() == null) {
            return null;
        }

        // mark this chest as a JustLootIt container
        UUID random = UUID.randomUUID();
        container.set(JustLootItKey.IDENTITY, UUIDTagType.TYPE, random);
        return random;
    }
}
