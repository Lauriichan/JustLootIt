package me.lauriichan.spigot.justlootit.config.data;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.loot.ILootCondition;
import me.lauriichan.spigot.justlootit.loot.ILootModifier;
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Ref;
import me.lauriichan.spigot.justlootit.util.LootHelper;

public record LootModification(NamespacedKey id, ILootCondition condition, ILootModifier modifier, ILootPoolProvider provider) {

    public boolean isApplicable(Container container, PlayerAdapter player, Location location, NamespacedKey lootTableKey) {
        return condition == null || condition.includes(container, player, location, lootTableKey);
    }

    public void apply(VersionHandler versionHandler, Inventory inventory, long seed) {
        if (modifier != null) {
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
        if (provider != null) {
            ObjectArrayList<ItemStack> additionalItems = new ObjectArrayList<>();
            Random random = new Random(seed);
            provider.provideLoot(versionHandler, random, additionalItems);
            LootHelper.mergeContents(additionalItems, inventory);
            inventory.clear();
            LootHelper.scrambleInto(additionalItems, inventory, seed);
        }
    }

    public void apply(VersionHandler versionHandler, ObjectArrayList<ItemStack> items, long seed) {
        if (modifier != null) {
            Ref<ItemStack> itemRef = Ref.of();
            Ref<ItemMeta> metaRef = Ref.of();
            Random random = new Random(seed);
            for (int i = 0; i < items.size(); i++) {
                ItemStack itemStack = items.get(i);
                if (itemStack == null || itemStack.getType().isAir()) {
                    continue;
                }
                itemRef.set(itemStack);
                metaRef.set(itemStack.getItemMeta());
                modifier.modify(versionHandler, random, itemRef, metaRef);
                if (itemStack != itemRef.get()) {
                    itemStack = itemRef.get();
                    itemStack.setItemMeta(metaRef.get());
                    items.set(i, itemStack);
                    continue;
                }
                itemStack.setItemMeta(metaRef.get());
            }
        }
        if (provider != null) {
            provider.provideLoot(versionHandler, new Random(seed), items);
        }
    }

}
