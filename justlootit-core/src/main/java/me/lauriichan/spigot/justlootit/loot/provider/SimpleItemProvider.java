package me.lauriichan.spigot.justlootit.loot.provider;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.loot.ILootItemProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public record SimpleItemProvider(Material material, int amount) implements ILootItemProvider {

    @Override
    public ItemStack createItem(VersionHandler versionHandler, Random random) {
        ItemStack itemStack = new ItemStack(material);
        itemStack.setAmount(amount);
        return itemStack;
    }

}
