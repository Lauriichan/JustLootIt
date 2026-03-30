package me.lauriichan.spigot.justlootit.loot.modify.modifier;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.loot.modify.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record CombinedFunc(ObjectList<ILootModifierFunc> functions) implements ILootModifierFunc {

    @Override
    public void modify(Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        for (ILootModifierFunc function : functions) {
            function.modify(random, itemRef, metaRef);
        }
    }

}
