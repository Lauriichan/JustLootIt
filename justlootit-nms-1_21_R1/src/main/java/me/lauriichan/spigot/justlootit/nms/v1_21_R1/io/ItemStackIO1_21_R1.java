package me.lauriichan.spigot.justlootit.nms.v1_21_R1.io;

import java.lang.invoke.MethodHandle;

import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;

import me.lauriichan.laylib.reflection.ClassUtil;
import me.lauriichan.laylib.reflection.JavaLookup;
import me.lauriichan.spigot.justlootit.nms.v1_21_R1.util.NmsHelper1_21_R1;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;

public final class ItemStackIO1_21_R1 extends NbtIO1_21_R1<org.bukkit.inventory.ItemStack, CompoundTag> {

    public static final ItemStackIO1_21_R1 ITEM_STACK = new ItemStackIO1_21_R1();

    private static final MethodHandle CraftItemStack_handle = JavaLookup.PLATFORM.unreflectGetter(ClassUtil.getField(CraftItemStack.class, "handle"));
    
    private final Frozen registry = NmsHelper1_21_R1.getServer().registryAccess();

    private ItemStackIO1_21_R1() {
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
    
    @Override
    public CompoundTag upgradeNbt(DataFixer fixer, CompoundTag tag, int tagVersion, int serverVersion) {
        return (CompoundTag) fixer.update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, tag), tagVersion, serverVersion).getValue();
    }

}