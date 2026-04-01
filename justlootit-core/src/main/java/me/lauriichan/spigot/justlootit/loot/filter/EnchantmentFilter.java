package me.lauriichan.spigot.justlootit.loot.filter;

import java.util.Random;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.loot.ILootFilter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public record EnchantmentFilter(Enchantment enchantment, int min, int max) implements ILootFilter {

    @Override
    public boolean includes(VersionHandler versionHandler, Random random, ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        int level = itemStack.getItemMeta().getEnchantLevel(enchantment);
        return min >= level && level <= max;
    }

}
