package me.lauriichan.spigot.justlootit.nms.v1_19_R3.io;

import java.lang.invoke.MethodHandle;

import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ItemStackIO1_19_R3 extends NbtIO1_19_R3<org.bukkit.inventory.ItemStack, CompoundTag> {

    public static final ItemStackIO1_19_R3 ITEM_STACK = new ItemStackIO1_19_R3();

    private final MethodHandle CraftItemStack_handle = JavaAccess.accessFieldGetter(ClassUtil.getField(CraftItemStack.class, "handle"));

    private ItemStackIO1_19_R3() {
        super(org.bukkit.inventory.ItemStack.class, CompoundTag.TYPE);
    }

    @Override
    public CompoundTag asNbt(final org.bukkit.inventory.ItemStack value) {
        final ItemStack itemStack = asMinecraftStack(value);
        final CompoundTag tag = new CompoundTag();
        if (itemStack == ItemStack.EMPTY) {
            return tag;
        }
        return itemStack.save(tag);
    }

    @Override
    public org.bukkit.inventory.ItemStack fromNbt(final CompoundTag tag) {
        return CraftItemStack.asCraftMirror(tag.isEmpty() ? ItemStack.EMPTY : ItemStack.of(tag));
    }

    private ItemStack asMinecraftStack(final org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null) {
            return ItemStack.EMPTY;
        }
        if (itemStack instanceof CraftItemStack) {
            final Object handle = JavaAccess.invoke(itemStack, CraftItemStack_handle);
            if (handle == null) {
                return ItemStack.EMPTY;
            }
            return (ItemStack) handle;
        }
        if (itemStack == null || itemStack.getType() == org.bukkit.Material.AIR) {
            return ItemStack.EMPTY;
        }
        @SuppressWarnings("deprecation")
        final Item item = CraftMagicNumbers.getItem(itemStack.getType(), itemStack.getDurability());
        if (item == null) {
            return ItemStack.EMPTY;
        }
        final ItemStack stack = new ItemStack(item, itemStack.getAmount());
        if (itemStack.hasItemMeta()) {
            CraftItemStack.setItemMeta(stack, itemStack.getItemMeta());
        }
        return stack;
    }

}