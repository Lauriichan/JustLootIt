package me.lauriichan.spigot.justlootit.nms.nbt;

public abstract class NbtHelper {
    
    public abstract ICompoundTag createCompound();
    
    public abstract <T> IListTag<T> createList(TagType<T> type);

}
