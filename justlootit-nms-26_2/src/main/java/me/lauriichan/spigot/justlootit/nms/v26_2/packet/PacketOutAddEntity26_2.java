package me.lauriichan.spigot.justlootit.nms.v26_2.packet;

import java.util.UUID;

import org.bukkit.Registry;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;

import me.lauriichan.spigot.justlootit.nms.packet.PacketOutAddEntity;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityType;

public class PacketOutAddEntity26_2 extends PacketOutAddEntity {

    private final ClientboundAddEntityPacket packet;

    public PacketOutAddEntity26_2(final ClientboundAddEntityPacket packet) {
        this.packet = packet;
    }

    @Override
    public Object asMinecraft() {
        return packet;
    }

    @Override
    public int getEntityId() {
        return packet.getId();
    }

    @Override
    public UUID getEntityUniqueId() {
        return packet.getUUID();
    }

    @Override
    public org.bukkit.entity.EntityType getEntityType() {
        return Registry.ENTITY_TYPE.get(CraftNamespacedKey.fromMinecraft(EntityType.getKey(packet.getType())));
    }

}