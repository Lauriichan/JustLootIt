package me.lauriichan.spigot.justlootit.nms.v1_21_R7.nbt;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.nms.nbt.ICompoundTag;
import me.lauriichan.spigot.justlootit.nms.nbt.IListTag;
import me.lauriichan.spigot.justlootit.nms.nbt.NbtHelper;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.nms.v1_21_R7.io.ItemStackIO1_21_R7;
import me.lauriichan.spigot.justlootit.nms.v1_21_R7.util.NmsHelper1_21_R7;
import net.minecraft.nbt.CompoundTag;

public final class NbtHelper1_21_R7 extends NbtHelper {

    @Override
    public ICompoundTag createCompound() {
        return new CompoundTag1_21_R7();
    }

    @Override
    public <T> IListTag<T> createList(TagType<T> type) {
        return new ListTag1_21_R7<>(type);
    }
    
    @Override
    public ICompoundTag asTag(ItemStack itemStack) {
        return new CompoundTag1_21_R7(ItemStackIO1_21_R7.ITEM_STACK.asNbt(itemStack));
    }

    @Override
    public ICompoundTag getCustomDataTag(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        CompoundTag tag = NmsHelper1_21_R7.getCustomData(itemStack.getItemMeta());
        if (tag == null) {
            NmsHelper1_21_R7.setCustomData(meta, tag = new CompoundTag());
        }
        return new CompoundTag1_21_R7(tag);
    }
    
    @Override
    public ItemStack asItem(ICompoundTag tag) {
        return ItemStackIO1_21_R7.ITEM_STACK.fromNbt(((CompoundTag1_21_R7) tag).handle());
    }

}
