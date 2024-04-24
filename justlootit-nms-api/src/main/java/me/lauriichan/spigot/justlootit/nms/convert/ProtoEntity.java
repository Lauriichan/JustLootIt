package me.lauriichan.spigot.justlootit.nms.convert;

import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;

public abstract class ProtoEntity {
    
    public abstract EntityType getType();
    
    public abstract PersistentDataContainer getContainer();

}
