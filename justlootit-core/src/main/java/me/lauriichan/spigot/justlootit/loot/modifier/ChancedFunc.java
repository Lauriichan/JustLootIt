package me.lauriichan.spigot.justlootit.loot.modifier;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.loot.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record ChancedFunc(ILootModifierFunc func, int threshold, int bound) implements ILootModifierFunc {

    @Override
    public void modify(VersionHandler versionHandler, Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        if (random.nextInt(bound) <= threshold) {
            func.modify(versionHandler, random, itemRef, metaRef);
        }
    }

}
