package me.lauriichan.spigot.justlootit.loot.filter;

import java.util.Random;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.loot.ILootFilter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record EnchantmentFilter(Enchantment enchantment, int min, int max) implements ILootFilter {

    @Override
    public boolean includes(VersionHandler versionHandler, Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        int level = metaRef.get().getEnchantLevel(enchantment);
        return min >= level && level <= max;
    }

}
