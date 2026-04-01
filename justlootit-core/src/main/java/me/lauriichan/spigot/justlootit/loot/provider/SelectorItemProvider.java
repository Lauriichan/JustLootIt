package me.lauriichan.spigot.justlootit.loot.provider;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.loot.ILootItemProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.util.WeightedList;

public record SelectorItemProvider(WeightedList<ILootItemProvider> providers) implements ILootItemProvider {

    @Override
    public ItemStack createItem(VersionHandler versionHandler, Random random) {
        return providers.randomItem(random).createItem(versionHandler, random);
    }

}
