package me.lauriichan.spigot.justlootit.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.listener.IPacketListener;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketHandler;

public class ItemFrameListener implements IPacketListener, Listener {
    
    // TODO: Rework to match new systems

    @PacketHandler
    public void onEntityMetadata(PlayerAdapter player, PacketOutSetEntityData packet) {
        Entity entity = player.getLevel().getBukkitEntityById(packet.getEntityId());
        if(entity == null) {
            return;
        }
        EntityType type = entity.getType();
        if(type != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME) {
            return;
        }
    }

//    @EventHandler(ignoreCancelled = true)
//    public void onInteractEvent(PlayerInteractEntityEvent event) {
//        Entity entity = event.getRightClicked();
//        if (entity.getType() != EntityType.ITEM_FRAME) {
//            return;
//        }
//        event.setCancelled(entity.getPersistentDataContainer().has(JustLootItKey.IDENTITY, PersistentDataType.BYTE));
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    public void onHitEvent(EntityDamageByEntityEvent event) {
//        Entity damager = event.getDamager();
//        if (damager.getType() != EntityType.PLAYER) {
//            return;
//        }
//        Entity entity = event.getEntity();
//        if (entity.getType() != EntityType.ITEM_FRAME) {
//            return;
//        }
//        PersistentDataContainer container = entity.getPersistentDataContainer();
//        event.setCancelled(container.has(JustLootItKey.IDENTITY, PersistentDataType.BYTE));
//        if (!event.isCancelled()) {
//            return;
//        }
//        NamespacedKey playerKey = JustLootItKey.keyOf(damager.getUniqueId());
//        if (container.has(playerKey, PersistentDataType.BYTE)) {
//            return;
//        }
//        container.set(playerKey, PersistentDataType.BYTE, JustLootItKey.TRUE);
//        damager.getWorld().dropItem(entity.getLocation(), ((ItemFrame) entity).getItem().clone());
//    }
//
//    @EventHandler(ignoreCancelled = true)
//    public void onHangingBreak(HangingBreakEvent event) {
//        Entity entity = event.getEntity();
//        EntityType type = entity.getType();
//        if (type != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME) {
//            return;
//        }
//        event.setCancelled(entity.getPersistentDataContainer().has(JustLootItKey.IDENTITY, PersistentDataType.BYTE));
//    }

}
