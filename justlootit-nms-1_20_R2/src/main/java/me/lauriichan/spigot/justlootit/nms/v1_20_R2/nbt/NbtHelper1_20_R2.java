package me.lauriichan.spigot.justlootit.nms.v1_20_R2.nbt;

import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.NbtHelper;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.nms.v1_20_R2.io.ItemStackIO1_20_R2;

public final class NbtHelper1_20_R2 extends NbtHelper {

    @Override
    public ICompoundTag createCompound() {
        return new CompoundTag1_20_R2();
    }

    @Override
    public <T> IListTag<T> createList(TagType<T> type) {
        return new ListTag1_20_R2<>(type);
    }
    
    @Override
    public ICompoundTag asTag(ItemStack itemStack) {
        return new CompoundTag1_20_R2(ItemStackIO1_20_R2.ITEM_STACK.asMinecraftStack(itemStack).getOrCreateTag().copy());
    }
    
    @Override
    public ItemStack asItem(ICompoundTag tag) {
        return ItemStackIO1_20_R2.ITEM_STACK.fromNbt(((CompoundTag1_20_R2) tag).handle());
    }

}
