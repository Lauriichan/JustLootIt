package me.lauriichan.spigot.justlootit.nms.v1_19_R2.model;

import java.util.Objects;

import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;

import me.lauriichan.spigot.justlootit.nms.model.IItemEntityData;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.world.item.ItemStack;

public class ItemEntityData1_19_R2 extends EntityData1_19_R2 implements IItemEntityData {

    private final DataValue<ItemStack> value;
    private org.bukkit.inventory.ItemStack itemStack;
    private boolean changed = false;
    private boolean dirty = false;

    ItemEntityData1_19_R2(DataValue<ItemStack> value) {
        this.value = value;
        this.itemStack = CraftItemStack.asCraftMirror(value.value());
    }

    @Override
    public int getId() {
        return value.id();
    }

    @Override
    public org.bukkit.inventory.ItemStack getItem() {
        return itemStack;
    }

    @Override
    public void setItem(org.bukkit.inventory.ItemStack itemStack) {
        if (Objects.equals(this.itemStack, itemStack)) {
            return;
        }
        this.itemStack = itemStack;
        changed = true;
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public DataValue<?> build() {
        if (!changed) {
            return value;
        }
        dirty = false;
        return new DataValue<>(value.id(), value.serializer(), CraftItemStack.asNMSCopy(itemStack));
    }

}
