package me.lauriichan.spigot.justlootit.loot.modify.filter;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.loot.modify.ILootFilter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public record ChanceFilter(int threshold, int bound) implements ILootFilter {

    @Override
    public boolean includes(VersionHandler versionHandler, Random random, ItemStack itemStack) {
        return random.nextInt(bound) <= threshold;
    }

}
