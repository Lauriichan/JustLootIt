package me.lauriichan.spigot.justlootit.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import me.lauriichan.spigot.justlootit.JustLootItKey;
import me.lauriichan.spigot.justlootit.capability.StorageCapability;
import me.lauriichan.spigot.justlootit.data.FrameContainer;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.model.IEntityData;
import me.lauriichan.spigot.justlootit.nms.model.IItemEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutAddEntity;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData;
import me.lauriichan.spigot.justlootit.nms.packet.PacketOutSetEntityData.IEntityDataPack;
import me.lauriichan.spigot.justlootit.nms.packet.listener.IPacketListener;
import me.lauriichan.spigot.justlootit.nms.packet.listener.PacketHandler;
import me.lauriichan.spigot.justlootit.nms.util.argument.ArgumentMap;
import me.lauriichan.spigot.justlootit.storage.Storable;

public class ItemFramePacketListener implements IPacketListener {

    private final VersionHandler versionHandler;

    public ItemFramePacketListener(final VersionHandler versionHandler) {
        this.versionHandler = versionHandler;
    }
    
    @PacketHandler
    public void onEntityCreate(final PlayerAdapter player, final PacketOutAddEntity packet) {
        EntityType type = packet.getEntityType();
        if (type != EntityType.ITEM_FRAME && type != EntityType.GLOW_ITEM_FRAME) {
            return;
        }
        Entity entity = player.getLevel().getBukkitEntityById(packet.getEntityId());
        if (entity == null) {
            return;
        }
        final PersistentDataContainer container = entity.getPersistentDataContainer();
        if (!container.has(JustLootItKey.identity(), PersistentDataType.LONG)) {
            return;
        }
        final long id = container.get(JustLootItKey.identity(), PersistentDataType.LONG);
        player.getLevel().getCapability(StorageCapability.class).ifPresent(capability -> {
            final Storable storable = capability.storage().read(id);
            if (storable instanceof final FrameContainer frame) {
                if (!frame.canAccess(player.getUniqueId())) {
                    return;
                }
                final PacketOutSetEntityData dataPacket = versionHandler.packetManager().createPacket(new ArgumentMap().set("entity", entity), PacketOutSetEntityData.class);
                final IEntityDataPack pack = dataPacket.getData();
                final IEntityData data = pack.getById(8);
                if (!(data instanceof IItemEntityData)) {
                    return;
                }
                ((IItemEntityData) data).setItem(frame.getItem());
                versionHandler.mainService().submit(() -> player.send(dataPacket));
            }
        });
    }

}
