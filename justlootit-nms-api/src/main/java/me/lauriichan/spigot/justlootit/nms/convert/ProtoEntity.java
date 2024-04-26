package me.lauriichan.spigot.justlootit.nms.convert;

import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;

public abstract class ProtoEntity {
    
    public abstract EntityType getType();
    
    public abstract PersistentDataContainer getContainer();
    
    public abstract ICompoundTag getNbt();

}
