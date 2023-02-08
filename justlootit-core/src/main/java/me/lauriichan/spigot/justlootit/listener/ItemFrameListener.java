package me.lauriichan.spigot.justlootit.listener;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import me.lauriichan.spigot.justlootit.nms.model.IItemEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData.IEntityDataPack;
import me.lauriichan.spigot.justlootit.nms.packet.listener.IPacketListener;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketHandler;

public class ItemFrameListener implements IPacketListener, Listener {

    @PacketHandler
    public void onEntityMetadata(PlayerAdapter player, PacketOutSetEntityData packet) {
        Entity entity = player.getLevel().getBukkitEntityById(packet.getEntityId());
        if (entity.getType() != EntityType.ITEM_FRAME) {
            return;
        }
        IEntityDataPack pack = packet.getData();
        IEntityData data = pack.getById(8);
        if (!(data instanceof IItemEntityData)) {
            return;
        }
        IItemEntityData itemData = (IItemEntityData) data;
        ItemStack itemStack = itemData.getItem();
        if (itemStack.getType() != Material.ELYTRA) {
            return;
        }
        PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!container.has(JustLootItKey.IDENTITY, PersistentDataType.BYTE)
            || container.has(JustLootItKey.keyOf(player.getUniqueId()), PersistentDataType.BYTE)) {
            return;
        }
        itemData.setItem(new ItemStack(Material.AIR));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEvent(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (entity.getType() != EntityType.ITEM_FRAME) {
            return;
        }
        event.setCancelled(entity.getPersistentDataContainer().has(JustLootItKey.IDENTITY, PersistentDataType.BYTE));
    }

    @EventHandler(ignoreCancelled = true)
    public void onHitEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager.getType() != EntityType.PLAYER) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity.getType() != EntityType.ITEM_FRAME) {
            return;
        }
        PersistentDataContainer container = entity.getPersistentDataContainer();
        event.setCancelled(container.has(JustLootItKey.IDENTITY, PersistentDataType.BYTE));
        if (!event.isCancelled()) {
            return;
        }
        NamespacedKey playerKey = JustLootItKey.keyOf(damager.getUniqueId());
        if (container.has(playerKey, PersistentDataType.BYTE)) {
            return;
        }
        damager.getWorld().dropItem(entity.getLocation(), ((ItemFrame) entity).getItem().clone());
    }

}
