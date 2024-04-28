package me.lauriichan.spigot.justlootit.nms.nbt;

import org.bukkit.inventory.ItemStack;

public abstract class NbtHelper {
    
    public abstract ICompoundTag createCompound();
    
    public abstract <T> IListTag<T> createList(TagType<T> type);
    
    /*
     * To tag
     */
    
    public abstract ICompoundTag asTag(ItemStack itemStack);
    
    /*
     * From tag
     */
    
    public abstract ItemStack asItem(ICompoundTag tag);

}
