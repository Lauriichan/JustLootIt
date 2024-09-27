package me.lauriichan.spigot.justlootit.nms.v1_20_R2.util;

import java.util.Arrays;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public final class NonNullItemList1_20_R2 extends NonNullList<ItemStack> {

    public static NonNullItemList1_20_R2 of(List<ItemStack> list) {
        return of(list, ItemStack.EMPTY);
    }

    public static NonNullItemList1_20_R2 of(List<ItemStack> list, ItemStack defaultValue) {
        ObjectArrayList<ItemStack> newList = ObjectArrayList.wrap(new ItemStack[list.size()]);
        ItemStack[] array = newList.elements();
        for (int i = 0; i < array.length; i++) {
            ItemStack stack = list.get(i);
            if (stack == null) {
                array[i] = defaultValue;
                continue;
            }
            array[i] = stack;
        }
        return new NonNullItemList1_20_R2(newList, defaultValue);
    }

    public static NonNullItemList1_20_R2 of(int size) {
        return of(size, ItemStack.EMPTY);
    }

    public static NonNullItemList1_20_R2 of(int size, ItemStack defaultValue) {
        ObjectArrayList<ItemStack> list = ObjectArrayList.wrap(new ItemStack[size]);
        Arrays.fill(list.elements(), defaultValue);
        return new NonNullItemList1_20_R2(list, defaultValue);
    }

    private NonNullItemList1_20_R2(ObjectArrayList<ItemStack> list, ItemStack defaultValue) {
        super(list, defaultValue);

    }

}
