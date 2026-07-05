package me.lauriichan.spigot.justlootit.loot.provider;

import java.util.Random;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.data.CustomLootTable;
import me.lauriichan.spigot.justlootit.config.loot.LootTableDirectoryData;
import me.lauriichan.spigot.justlootit.loot.ILootItemProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public record ReferenceItemProvider(NamespacedKey lootTableKey) implements ILootItemProvider {

    @Override
    public ItemStack createItem(VersionHandler versionHandler, Random random) {
        CustomLootTable table = ((JustLootItPlugin) versionHandler.plugin()).dataManager().directoryData(LootTableDirectoryData.class)
            .get(lootTableKey);
        if (table == null) {
            return null;
        }
        ObjectArrayList<ItemStack> generatedItems = new ObjectArrayList<>();
        table.provider().provideLoot(versionHandler, random, generatedItems);
        if (generatedItems.isEmpty()) {
            return null;
        }
        return generatedItems.get(0);
    }

}
