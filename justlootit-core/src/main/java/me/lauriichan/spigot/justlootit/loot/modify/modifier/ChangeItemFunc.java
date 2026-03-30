package me.lauriichan.spigot.justlootit.loot.modify.modifier;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.loot.modify.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record ChangeItemFunc(ItemStack replacement) implements ILootModifierFunc {

    @Override
    public void modify(Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        int amount = itemRef.get().getAmount();
        ItemStack stack = replacement.clone();
        itemRef.set(stack);
        metaRef.set(stack.getItemMeta());
        stack.setAmount(amount);
    }

}
