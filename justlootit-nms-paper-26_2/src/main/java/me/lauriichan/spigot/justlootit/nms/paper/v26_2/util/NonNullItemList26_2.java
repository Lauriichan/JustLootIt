package me.lauriichan.spigot.justlootit.nms.paper.v26_2.util;

import java.util.Arrays;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public final class NonNullItemList26_2 extends NonNullList<ItemStack> {

    public static NonNullItemList26_2 of(List<ItemStack> list) {
        return of(list, ItemStack.EMPTY);
    }

    public static NonNullItemList26_2 of(List<ItemStack> list, ItemStack defaultValue) {
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
        return new NonNullItemList26_2(newList, defaultValue);
    }

    public static NonNullItemList26_2 of(int size) {
        return of(size, ItemStack.EMPTY);
    }

    public static NonNullItemList26_2 of(int size, ItemStack defaultValue) {
        ObjectArrayList<ItemStack> list = ObjectArrayList.wrap(new ItemStack[size]);
        Arrays.fill(list.elements(), defaultValue);
        return new NonNullItemList26_2(list, defaultValue);
    }

    private NonNullItemList26_2(ObjectArrayList<ItemStack> list, ItemStack defaultValue) {
        super(list, defaultValue);

    }

}
