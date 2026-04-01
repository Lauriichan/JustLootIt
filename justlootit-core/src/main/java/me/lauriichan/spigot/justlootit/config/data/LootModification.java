package me.lauriichan.spigot.justlootit.config.data;

import java.util.Random;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.loot.ILootCondition;
import me.lauriichan.spigot.justlootit.loot.ILootModifier;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record LootModification(NamespacedKey id, ILootCondition condition, ILootModifier modifier) {

    public boolean isApplicable(Container container, PlayerAdapter player, NamespacedKey lootTableKey) {
        return condition == null || condition.includes(container, player, lootTableKey);
    }

    public void apply(VersionHandler versionHandler, Inventory inventory, long seed) {
        int size = inventory.getSize();
        Random random = new Random(seed);
        Ref<ItemStack> itemRef = Ref.of();
        Ref<ItemMeta> metaRef = Ref.of();
        for (int i = 0; i < size; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack == null || itemStack.getType().isAir()) {
                continue;
            }
            itemRef.set(itemStack);
            metaRef.set(itemStack.getItemMeta());
            modifier.modify(versionHandler, random, itemRef, metaRef);
            if (itemStack != itemRef.get()) {
                itemStack = itemRef.get();
                itemStack.setItemMeta(metaRef.get());
                inventory.setItem(i, itemStack);
                continue;
            }
            itemStack.setItemMeta(metaRef.get());
        }
    }

    public ItemStack apply(VersionHandler versionHandler, ItemStack item, long seed) {
        Random random = new Random(seed);
        Ref<ItemStack> itemRef = Ref.of(item);
        Ref<ItemMeta> metaRef = Ref.of(item.getItemMeta());
        modifier.modify(versionHandler, random, itemRef, metaRef);
        item = itemRef.get();
        item.setItemMeta(metaRef.get());
        return item;
    }

}
