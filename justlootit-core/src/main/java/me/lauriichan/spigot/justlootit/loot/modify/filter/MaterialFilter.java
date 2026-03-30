package me.lauriichan.spigot.justlootit.loot.modify.filter;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.loot.modify.ILootFilter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public record MaterialFilter(Material material) implements ILootFilter {

    @Override
    public boolean includes(VersionHandler versionHandler, Random random, ItemStack itemStack) {
        return itemStack.getType() == material;
    }

}
