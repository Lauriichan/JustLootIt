package me.lauriichan.spigot.justlootit.loot;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public interface ILootPoolProvider {

    void provideLoot(VersionHandler versionHandler, Random random, ObjectList<ItemStack> items);

}
