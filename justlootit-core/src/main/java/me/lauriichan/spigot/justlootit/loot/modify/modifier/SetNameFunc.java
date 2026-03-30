package me.lauriichan.spigot.justlootit.loot.modify.modifier;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.minecraft.pluginbase.message.component.ComponentBuilder;
import me.lauriichan.spigot.justlootit.loot.modify.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record SetNameFunc(String newNameText) implements ILootModifierFunc {

    @Override
    public void modify(Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        if (metaRef.isEmpty()) {
            return;
        }
        metaRef.get().setDisplayName(ComponentBuilder.parse(newNameText).asLegacyText());
    }

}
