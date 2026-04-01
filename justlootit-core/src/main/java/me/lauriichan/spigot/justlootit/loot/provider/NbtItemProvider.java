package me.lauriichan.spigot.justlootit.loot.provider;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.loot.ILootItemProvider;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public record NbtItemProvider(ItemStack parsedStack) implements ILootItemProvider {

    @Override
    public ItemStack createItem(VersionHandler versionHandler, Random random) {
        return parsedStack.clone();
    }

}
