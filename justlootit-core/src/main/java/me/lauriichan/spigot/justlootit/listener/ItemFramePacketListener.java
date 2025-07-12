package me.lauriichan.spigot.justlootit.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.spigot.justlootit.JustLootItAccess;
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
import me.lauriichan.spigot.justlootit.storage.Stored;

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
        if (!JustLootItAccess.hasIdentity(container)) {
            return;
        }
        final long id = JustLootItAccess.getIdentity(container);
        player.getLevel().getCapability(StorageCapability.class).ifPresent(capability -> {
            final Stored<FrameContainer> stored = capability.storage().read(id);
            if (!stored.value().canAccess(player.getUniqueId())) {
                return;
            }
            final PacketOutSetEntityData dataPacket = versionHandler.packetManager().createPacket(new ArgumentMap().set("entity", entity),
                PacketOutSetEntityData.class);
            final IEntityDataPack pack = dataPacket.getData();
            final IEntityData data = pack.getById(versionHandler.versionHelper().getItemFrameItemDataId());
            if (!(data instanceof IItemEntityData)) {
                return;
            }
            ((IItemEntityData) data).setItem(stored.value().getItem());
            versionHandler.platform().scheduler().sync(() -> player.send(dataPacket));
        });
    }

}
