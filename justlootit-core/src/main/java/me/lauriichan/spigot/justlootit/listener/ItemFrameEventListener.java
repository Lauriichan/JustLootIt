package me.lauriichan.spigot.justlootit.listener;

import java.time.OffsetDateTime;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.lauriichan.minecraft.pluginbase.extension.Extension;
import me.lauriichan.minecraft.pluginbase.listener.IListenerExtension;
import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import me.lauriichan.spigot.justlootit.nms.model.IItemEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData.IEntityDataPack;
import me.lauriichan.spigot.justlootit.nms.util.argument.ArgumentMap;
import me.lauriichan.spigot.justlootit.storage.Storable;
import me.lauriichan.spigot.justlootit.util.persistence.BreakData;

@Extension
public class ItemFrameEventListener implements IListenerExtension {

    private final VersionHandler versionHandler;

    public ItemFrameEventListener(final JustLootItPlugin plugin) {
        this.versionHandler = plugin.versionHandler();
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEvent(final PlayerInteractEntityEvent event) {
        final Entity entity = event.getRightClicked();
        EntityType type;
        if (entity == null || (type = entity.getType()) != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME) {
            return;
        }
        final PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!container.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(final HangingBreakEvent event) {
        final Entity entity = event.getEntity();
        EntityType type;
        if (entity == null || (type = entity.getType()) != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME) {
            return;
        }
        event.setCancelled(entity.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG));
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreakByEntity(final HangingBreakByEntityEvent event) {
        final Entity entity = event.getEntity();
        EntityType type;
        if (entity == null || (type = entity.getType()) != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME) {
            return;
        }
        final PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!container.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        event.setCancelled(true);
        final Entity remover = event.getRemover();
        if (remover.getType() != EntityType.PLAYER) {
            return;
        }
        final Player player = (Player) remover;
        if (player.hasPermission("" /* TODO: Add permission here */) && player.isSneaking()) {
            final BreakData data = container.getOrDefault(JustLootItKey.breakData(), BreakData.BREAK_DATA_TYPE, null);
            if (data == null || !data.playerId().equals(player.getUniqueId()) || data.time().isBefore(OffsetDateTime.now())) {
                // TODO: Send message to repeat
                container.set(JustLootItKey.breakData(), BreakData.BREAK_DATA_TYPE,
                    new BreakData(player.getUniqueId(), OffsetDateTime.now().plusMinutes(2)));
                player.sendMessage("Hit again in 2 mins!");
                return;
            }
            final long id = container.get(JustLootItKey.identity(), PersistentDataType.LONG);
            versionHandler.getLevel(entity.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
                if (!capability.storage().delete(id)) {
                    // TODO: Send info that container wasn't available
                    player.sendMessage("No container removed");
                }
                container.remove(JustLootItKey.identity());
                container.remove(JustLootItKey.breakData());
                // TODO: Send successfully removed message
                player.sendMessage("Removed");
                final PacketOutSetEntityData packet = versionHandler.packetManager().createPacket(new ArgumentMap().set("entity", entity),
                    PacketOutSetEntityData.class);
                versionHandler.broadcast(packet);
            });
            return;
        }
        final long id = container.get(JustLootItKey.identity(), PersistentDataType.LONG);
        versionHandler.getLevel(entity.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            final Storable storable = capability.storage().read(id);
            if (storable instanceof final FrameContainer frame) {
                if (!frame.access(player.getUniqueId())) {
                    return;
                }
                final ItemStack itemStack = frame.getItem().clone();
                if (!player.getInventory().addItem(itemStack).isEmpty()) {
                    player.getWorld().dropItemNaturally(entity.getLocation(), itemStack);
                }
                player.playSound(entity.getLocation(),
                    type == EntityType.GLOW_ITEM_FRAME ? Sound.ENTITY_GLOW_ITEM_FRAME_REMOVE_ITEM : Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM,
                    SoundCategory.NEUTRAL, 1f, 1f);
                final PacketOutSetEntityData packet = versionHandler.packetManager().createPacket(new ArgumentMap().set("entity", entity),
                    PacketOutSetEntityData.class);
                if (packet != null) {
                    final IEntityDataPack pack = packet.getData();
                    final IEntityData data = pack.getById(8);
                    if (data instanceof final IItemEntityData itemData) {
                        itemData.setItem(null);
                        versionHandler.getPlayer(player).send(packet);
                    }
                }
            }
        });
    }

}
