package me.lauriichan.spigot.justlootit.loot.provider;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.loot.ILootPoolProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public record ChancedPoolProvider(ILootPoolProvider poolProvider, int threshold, int bound) implements ILootPoolProvider {

    @Override
    public void provideLoot(VersionHandler versionHandler, Random random, ObjectList<ItemStack> items) {
        if (random.nextInt(bound) <= threshold) {
            poolProvider.provideLoot(versionHandler, random, items);
        }
    }

}
