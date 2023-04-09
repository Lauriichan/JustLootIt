package me.lauriichan.spigot.justlootit.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import me.lauriichan.spigot.justlootit.nms.model.IItemEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData.IEntityDataPack;
import me.lauriichan.spigot.justlootit.nms.packet.listener.IPacketListener;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketHandler;
import me.lauriichan.spigot.justlootit.nms.util.argument.ArgumentMap;
import me.lauriichan.spigot.justlootit.storage.Storable;

public class ItemFrameListener implements IPacketListener, Listener {

    // TODO: Make item frame containers breakable

    private final VersionHandler versionHandler;

    public ItemFrameListener(final VersionHandler versionHandler) {
        this.versionHandler = versionHandler;
    }

    @PacketHandler
    public void onEntityMetadata(PlayerAdapter player, PacketOutSetEntityData packet) {
        Entity entity = player.getLevel().getBukkitEntityById(packet.getEntityId());
        EntityType type;
        if (entity == null || ((type = entity.getType()) != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME)) {
            return;
        }
        PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!container.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        long id = container.get(JustLootItKey.identity(), PersistentDataType.LONG);
        player.getLevel().getCapability(StorageCapability.class).ifPresent(capability -> {
            IEntityDataPack pack = packet.getData();
            IEntityData data = pack.getById(8);
            if (!(data instanceof IItemEntityData)) {
                return;
            }
            IItemEntityData itemData = (IItemEntityData) data;
            Storable storable = capability.storage().read(id);
            if (storable instanceof FrameContainer frame) {
                if (!frame.canAccess(player.getUniqueId())) {
                    itemData.setItem(null);
                    return;
                }
                itemData.setItem(frame.getItem());
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEvent(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        EntityType type;
        if (entity == null || ((type = entity.getType()) != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME)) {
            return;
        }
        PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!container.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        long id = container.get(JustLootItKey.identity(), PersistentDataType.LONG);
        versionHandler.getLevel(entity.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            Storable storable = capability.storage().read(id);
            if (storable instanceof FrameContainer frame) {
                if (!frame.access(player.getUniqueId())) {
                    return;
                }
                ItemStack itemStack = frame.getItem().clone();
                if (!player.getInventory().addItem(itemStack).isEmpty()) {
                    player.getWorld().dropItemNaturally(entity.getLocation(), itemStack);
                }
                PacketOutSetEntityData packet = versionHandler.packetManager().createPacket(new ArgumentMap().set("entity", entity), PacketOutSetEntityData.class);
                if(packet != null) {
                    IEntityDataPack pack = packet.getData();
                    IEntityData data = pack.getById(8);
                    if (data instanceof IItemEntityData itemData) {
                        itemData.setItem(null);
                        versionHandler.getPlayer(player).send(packet);
                    }
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onHitEvent(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        EntityType type;
        if (entity == null || ((type = entity.getType()) != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME)) {
            return;
        }
        PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!container.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        event.setCancelled(true);
        Entity damager = event.getDamager();
        if (damager.getType() != EntityType.PLAYER) {
            return;
        }
        Player player = (Player) damager;
        long id = container.get(JustLootItKey.identity(), PersistentDataType.LONG);
        versionHandler.getLevel(entity.getWorld()).getCapability(StorageCapability.class).ifPresent(capability -> {
            Storable storable = capability.storage().read(id);
            if (storable instanceof FrameContainer frame) {
                if (!frame.access(player.getUniqueId())) {
                    return;
                }
                ItemStack itemStack = frame.getItem().clone();
                if (!player.getInventory().addItem(itemStack).isEmpty()) {
                    player.getWorld().dropItemNaturally(entity.getLocation(), itemStack);
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        Entity entity = event.getEntity();
        EntityType type;
        if (entity == null || ((type = entity.getType()) != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME)) {
            return;
        }
        event.setCancelled(entity.getPersistentDataContainer().has(JustLootItKey.identity(), PersistentDataType.LONG));
    }

}
