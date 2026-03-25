package me.lauriichan.spigot.justlootit.nms.v26_1.nbt;

import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.NbtHelper;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.nms.v26_1.io.ItemStackIO26_1;

public final class NbtHelper26_1 extends NbtHelper {

    @Override
    public ICompoundTag createCompound() {
        return new CompoundTag26_1();
    }

    @Override
    public <T> IListTag<T> createList(TagType<T> type) {
        return new ListTag26_1<>(type);
    }
    
    @Override
    public ICompoundTag asTag(ItemStack itemStack) {
        return new CompoundTag26_1(ItemStackIO26_1.ITEM_STACK.asNbt(itemStack));
    }
    
    @Override
    public ItemStack asItem(ICompoundTag tag) {
        return ItemStackIO26_1.ITEM_STACK.fromNbt(((CompoundTag26_1) tag).handle());
    }

}
