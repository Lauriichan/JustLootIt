package me.lauriichan.spigot.justlootit.config.data;

import java.util.Collection;
import java.util.Random;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.util.LootHelper;

public record CustomLootTable(VersionHandler versionHandler, NamespacedKey id, ILootPoolProvider provider) implements LootTable {

    @Override
    public NamespacedKey getKey() {
        return id;
    }

    @Override
    public Collection<ItemStack> populateLoot(Random random, LootContext context) {
        ObjectArrayList<ItemStack> list = new ObjectArrayList<>();
        try {
            provider.provideLoot(versionHandler, random, list);
        } catch (RuntimeException re) {
            versionHandler.logger().warning("Failed to generate loot using custom loot table '{0}'", re, id.toString());
        }
        return list;
    }

    @Override
    public void fillInventory(Inventory inventory, Random random, LootContext context) {
        ObjectArrayList<ItemStack> list = new ObjectArrayList<>();
        try {
            provider.provideLoot(versionHandler, random, list);
        } catch (RuntimeException re) {
            versionHandler.logger().warning("Failed to generate loot using custom loot table '{0}'", re, id.toString());
        }
        LootHelper.scrambleInto(list, inventory, random.nextLong());
    }

}
