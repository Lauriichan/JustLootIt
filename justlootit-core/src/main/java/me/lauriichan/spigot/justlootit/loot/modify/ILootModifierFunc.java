package me.lauriichan.spigot.justlootit.loot.modify;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.nms.util.Ref;

public interface ILootModifierFunc {
    
    void modify(Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef);

}
