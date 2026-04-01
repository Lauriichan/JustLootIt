package me.lauriichan.spigot.justlootit.loot;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public interface ILootItemProvider extends ILootPoolProvider {

    ItemStack createItem(VersionHandler versionHandler, Random random);

    @Override
    default void provideLoot(VersionHandler versionHandler, Random random, ObjectList<ItemStack> items) {
        ItemStack itemStack = createItem(versionHandler, random);
        if (itemStack == null) {
            return;
        }
        items.add(itemStack);
    }

}
