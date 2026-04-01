package me.lauriichan.spigot.justlootit.config.data;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public record CustomLootTable(VersionHandler versionHandler, NamespacedKey id, ILootPoolProvider provider) implements LootTable {

    @Override
    public NamespacedKey getKey() {
        return id;
    }

    @Override
    public Collection<ItemStack> populateLoot(Random random, LootContext context) {
        ObjectArrayList<ItemStack> list = new ObjectArrayList<>();
        provider.provideLoot(versionHandler, random, list);
        return list;
    }

    @Override
    public void fillInventory(Inventory inventory, Random random, LootContext context) {
        ObjectArrayList<ItemStack> list = new ObjectArrayList<>();
        provider.provideLoot(versionHandler, random, list);
        int size = inventory.getSize();
        if (list.size() > size) {
            list.trim(size);
        }
        int freeSlots = size - list.size();
        if (freeSlots > 0) {
            Random spreadRandom = new Random(random.nextLong());
            int spread = spreadRandom.nextInt(freeSlots + 1);
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
                ItemStack itemStack = validItems.get(spreadRandom.nextInt(validItems.size()));
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
        Collections.shuffle(list, random);
        for (ItemStack itemStack : list) {
            inventory.setItem(slots.popInt(), itemStack);
        }
    }

}
