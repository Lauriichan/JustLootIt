package me.lauriichan.spigot.justlootit.nms.paper.v26_2.nbt;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.NbtHelper;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.nms.paper.v26_2.io.ItemStackIO26_2;
import me.lauriichan.spigot.justlootit.nms.paper.v26_2.util.NmsHelper26_2;
import net.minecraft.nbt.CompoundTag;

public final class NbtHelper26_2 extends NbtHelper {

    @Override
    public ICompoundTag createCompound() {
        return new CompoundTag26_2();
    }

    @Override
    public <T> IListTag<T> createList(TagType<T> type) {
        return new ListTag26_2<>(type);
    }
    
    @Override
    public ICompoundTag asTag(ItemStack itemStack) {
        return new CompoundTag26_2(ItemStackIO26_2.ITEM_STACK.asNbt(itemStack));
    }

    @Override
    public ICompoundTag getCustomDataTag(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        CompoundTag tag = NmsHelper26_2.getCustomData(itemStack.getItemMeta());
        if (tag == null) {
            NmsHelper26_2.setCustomData(meta, tag = new CompoundTag());
        }
        return new CompoundTag26_2(tag);
    }
    
    @Override
    public ItemStack asItem(ICompoundTag tag) {
        return ItemStackIO26_2.ITEM_STACK.fromNbt(((CompoundTag26_2) tag).handle());
    }

}
