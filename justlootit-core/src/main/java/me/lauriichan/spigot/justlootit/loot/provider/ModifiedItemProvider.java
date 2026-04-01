package me.lauriichan.spigot.justlootit.loot.provider;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.spigot.justlootit.loot.ILootItemProvider;
import me.lauriichan.spigot.justlootit.loot.ILootModifier;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record ModifiedItemProvider(ILootItemProvider itemProvider, ObjectList<ILootModifier> modifiers) implements ILootItemProvider {

    @Override
    public ItemStack createItem(VersionHandler versionHandler, Random random) {
        Ref<ItemStack> itemRef = Ref.of(itemProvider.createItem(versionHandler, random));
        Ref<ItemMeta> metaRef = Ref.of(itemRef.get().getItemMeta());
        for (ILootModifier modifier : modifiers) {
            modifier.modify(versionHandler, random, itemRef, metaRef);
        }
        ItemStack itemStack = itemRef.get();
        itemStack.setItemMeta(metaRef.get());
        return itemStack;
    }

}
