package me.lauriichan.spigot.justlootit.loot.provider;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.util.WeightedList;

public record SelectorPoolProvider(WeightedList<ILootPoolProvider> providers, int minRolls, int maxRolls) implements ILootPoolProvider {

    @Override
    public void provideLoot(VersionHandler versionHandler, Random random, ObjectList<ItemStack> items) {
        int rolls = random.nextInt(minRolls, maxRolls + 1);
        while (rolls-- > 0) {
            providers.randomItem(random).provideLoot(versionHandler, random, items);
        }
    }

}
