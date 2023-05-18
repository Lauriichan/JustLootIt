package me.lauriichan.spigot.justlootit.nms.packet;

import java.util.UUID;

import org.bukkit.entity.EntityType;

public abstract class PacketOutAddEntity extends AbstractPacketOut {

    public abstract int getEntityId();
    
    public abstract UUID getEntityUniqueId();
    
    public abstract EntityType getEntityType();

}
