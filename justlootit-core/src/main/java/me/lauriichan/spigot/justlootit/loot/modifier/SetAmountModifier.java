package me.lauriichan.spigot.justlootit.loot.modifier;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.loot.ILootModifier;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record SetAmountModifier(int min, int max) implements ILootModifier {

    @Override
    public void modify(VersionHandler versionHandler, Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        itemRef.get().setAmount(min == max ? min : random.nextInt(min, max + 1));
    }

}
