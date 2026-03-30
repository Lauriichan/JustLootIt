package me.lauriichan.spigot.justlootit.loot.modify.modifier;

import java.util.Random;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.loot.modify.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record AddEnchantmentFunc(Enchantment enchantment, int level, int minLevel, int maxLevel, boolean ignoreRestrictions)
    implements ILootModifierFunc {

    @Override
    public void modify(Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        if (metaRef.isEmpty()) {
            return;
        }
        ItemMeta meta = metaRef.get();
        int current = meta.getEnchantLevel(enchantment) + level;
        meta.removeEnchant(enchantment);
        if (current < minLevel) {
            current = minLevel;
        } else if (current > maxLevel) {
            current = maxLevel;
        }
        meta.addEnchant(enchantment, current, ignoreRestrictions);
    }

}
