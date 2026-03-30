package me.lauriichan.spigot.justlootit.loot.modify.modifier;

import java.util.Random;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.loot.modify.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record SetEnchantmentFunc(Enchantment enchantment, int level) implements ILootModifierFunc {

    @Override
    public void modify(Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        if (metaRef.isEmpty()) {
            return;
        }
        ItemMeta meta = metaRef.get();
        meta.removeEnchant(enchantment);
        meta.addEnchant(enchantment, level, true);
    }

}
