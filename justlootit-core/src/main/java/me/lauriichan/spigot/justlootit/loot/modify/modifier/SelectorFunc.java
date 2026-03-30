package me.lauriichan.spigot.justlootit.loot.modify.modifier;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.loot.modify.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.nms.util.Ref;
import me.lauriichan.spigot.justlootit.util.WeightedList;

public record SelectorFunc(WeightedList<ILootModifierFunc> functions) implements ILootModifierFunc {

    @Override
    public void modify(Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        functions.randomItem(random).modify(random, itemRef, metaRef);
    }

}
