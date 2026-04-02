package me.lauriichan.spigot.justlootit.util;

import java.util.Collections;
import java.util.Random;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public final class LootHelper {

    public static void scrambleInto(ObjectArrayList<ItemStack> list, Inventory inventory, long scrambleSeed) {
        inventory.clear();
        int size = inventory.getSize();
        if (list.size() > size) {
            list.trim(size);
        }
        int freeSlots = size - list.size();
        Random random = new Random(scrambleSeed);
        if (freeSlots > 0) {
            int spread = random.nextInt(freeSlots + 1);
            ObjectArrayList<ItemStack> validItems = new ObjectArrayList<>();
            for (ItemStack stack : list) {
                if (stack.getAmount() > 1) {
                    validItems.add(stack);
                }
            }
            if (spread > validItems.size()) {
                // Only spread half as much
                spread /= 2;
            }
            while (!validItems.isEmpty() && spread-- > 0) {
                ItemStack itemStack = validItems.get(random.nextInt(validItems.size()));
                int amount = itemStack.getAmount() / 2;
                itemStack.setAmount(itemStack.getAmount() - amount);
                if (itemStack.getAmount() <= 1) {
                    validItems.remove(itemStack);
                }
                itemStack = itemStack.clone();
                itemStack.setAmount(amount);
                list.add(itemStack);
            }
        }
        IntArrayList slots = new IntArrayList(size);
        for (int i = 0; i < size; i++) {
            slots.add(i);
        }
        Collections.shuffle(slots, random);
        for (ItemStack itemStack : list) {
            inventory.setItem(slots.popInt(), itemStack);
        }
    }

    public static void mergeContents(ObjectArrayList<ItemStack> items, Inventory inventory) {
        ObjectArrayList<ItemStack> input = new ObjectArrayList<>(items);
        items.clear();
        ItemStack[] invItems = inventory.getStorageContents();
        if (invItems != null) {
            Collections.addAll(input, invItems);
        }
        for (ItemStack item : input) {
            if (item == null || item.getType().isAir()) {
                continue;
            }
            ItemStack[] similar = items.stream().filter(i -> i.isSimilar(item)).toArray(ItemStack[]::new);
            if (similar.length != 0) {
                int amount = item.getAmount();
                for (ItemStack similarItem : similar) {
                    int space = similarItem.getMaxStackSize() - similarItem.getAmount();
                    if (space == 0) {
                        continue;
                    }
                    if (amount - space < 0) {
                        similarItem.setAmount(similarItem.getAmount() + amount);
                        amount = 0;
                        break;
                    }
                    similarItem.setAmount(similarItem.getMaxStackSize());
                    amount -= space;
                }
                if (amount <= 0) {
                    continue;
                }
                item.setAmount(amount);
            }
            items.add(item);
        }
    }

}
