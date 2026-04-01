package me.lauriichan.spigot.justlootit.loot.filter;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.loot.ILootFilter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record ChanceFilter(int threshold, int bound) implements ILootFilter {

    @Override
    public boolean includes(VersionHandler versionHandler, Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        return random.nextInt(bound) <= threshold;
    }

}
