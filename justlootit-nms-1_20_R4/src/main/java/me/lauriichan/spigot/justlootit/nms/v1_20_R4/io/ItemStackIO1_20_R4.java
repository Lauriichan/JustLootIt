package me.lauriichan.spigot.justlootit.nms.v1_20_R4.io;

import java.lang.invoke.MethodHandle;

import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaAccess;
import me.lauriichan.spigot.justlootit.nms.v1_20_R4.util.NmsHelper1_20_R4;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class ItemStackIO1_20_R4 extends NbtIO1_20_R4<org.bukkit.inventory.ItemStack, CompoundTag> {

    public static final ItemStackIO1_20_R4 ITEM_STACK = new ItemStackIO1_20_R4();

    private static final MethodHandle CraftItemStack_handle = JavaAccess.accessFieldGetter(ClassUtil.getField(CraftItemStack.class, "handle"));
    
    private final Frozen registry = NmsHelper1_20_R4.getServer().registryAccess();

    private ItemStackIO1_20_R4() {
        super(org.bukkit.inventory.ItemStack.class, CompoundTag.TYPE);
    }

    @Override
    public CompoundTag asNbt(final org.bukkit.inventory.ItemStack value) {
        final ItemStack itemStack = asMinecraftStack(value);
        final CompoundTag tag = new CompoundTag();
        if (itemStack == ItemStack.EMPTY) {
            return tag;
        }
        return (CompoundTag) itemStack.save(registry, tag);
    }

    @Override
    public org.bukkit.inventory.ItemStack fromNbt(final CompoundTag tag) {
        return CraftItemStack.asCraftMirror(tag.isEmpty() ? ItemStack.EMPTY : ItemStack.parseOptional(registry, tag));
    }

    public ItemStack asMinecraftStack(final org.bukkit.inventory.ItemStack itemStack) {
        if (itemStack == null) {
            return ItemStack.EMPTY;
        }
        if (itemStack instanceof CraftItemStack craftItem) {
            try {
                ItemStack handle = (ItemStack) CraftItemStack_handle.invoke(craftItem);
                if (handle == null) {
                    return ItemStack.EMPTY;
                }
                return handle;
            } catch (Throwable e) {
                return ItemStack.EMPTY;
            }
        }
        if (itemStack == null || itemStack.getType().isAir()) {
            return ItemStack.EMPTY;
        }
        return CraftItemStack.asNMSCopy(itemStack);
    }

}