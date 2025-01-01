package me.lauriichan.spigot.justlootit.nms.v1_21_R3.nbt;

import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.NbtHelper;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.nms.v1_21_R3.io.ItemStackIO1_21_R3;

public final class NbtHelper1_21_R3 extends NbtHelper {

    @Override
    public ICompoundTag createCompound() {
        return new CompoundTag1_21_R3();
    }

    @Override
    public <T> IListTag<T> createList(TagType<T> type) {
        return new ListTag1_21_R3<>(type);
    }
    
    @Override
    public ICompoundTag asTag(ItemStack itemStack) {
        return new CompoundTag1_21_R3(ItemStackIO1_21_R3.ITEM_STACK.asNbt(itemStack));
    }
    
    @Override
    public ItemStack asItem(ICompoundTag tag) {
        return ItemStackIO1_21_R3.ITEM_STACK.fromNbt(((CompoundTag1_21_R3) tag).handle());
    }

}
