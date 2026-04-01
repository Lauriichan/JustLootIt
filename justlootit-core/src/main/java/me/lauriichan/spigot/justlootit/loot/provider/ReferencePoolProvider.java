package me.lauriichan.spigot.justlootit.loot.provider;

import java.util.Random;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.data.CustomLootTable;
import me.lauriichan.spigot.justlootit.config.loot.LootTableDirectoryData;
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public record ReferencePoolProvider(NamespacedKey lootTableKey) implements ILootPoolProvider {

    @Override
    public void provideLoot(VersionHandler versionHandler, Random random, ObjectList<ItemStack> items) {
        CustomLootTable table = ((JustLootItPlugin) versionHandler.plugin()).dataManager().directoryData(LootTableDirectoryData.class)
            .get(lootTableKey);
        if (table == null) {
            return;
        }
        table.provider().provideLoot(versionHandler, random, items);
    }

}
