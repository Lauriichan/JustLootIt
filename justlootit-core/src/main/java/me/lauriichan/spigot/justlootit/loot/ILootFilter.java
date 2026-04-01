package me.lauriichan.spigot.justlootit.loot;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.nms.VersionHandler;

@FunctionalInterface
public interface ILootFilter {

    boolean includes(VersionHandler versionHandler, Random random, ItemStack itemStack);

}
