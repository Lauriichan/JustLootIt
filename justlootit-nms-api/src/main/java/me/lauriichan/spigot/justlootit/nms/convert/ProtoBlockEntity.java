package me.lauriichan.spigot.justlootit.nms.convert;

import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;

public abstract class ProtoBlockEntity extends ProtoBlock {

    public abstract ICompoundTag getNbt();

    public abstract PersistentDataContainer getContainer();
    
    public abstract boolean hasTileEntity();
    
    public abstract boolean hasInventory();
    
    public abstract Inventory getInventory();

}
