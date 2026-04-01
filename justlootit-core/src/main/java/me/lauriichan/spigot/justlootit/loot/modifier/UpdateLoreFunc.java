package me.lauriichan.spigot.justlootit.loot.modifier;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.minecraft.pluginbase.message.component.ComponentBuilder;
import me.lauriichan.spigot.justlootit.loot.ILootModifierFunc;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record UpdateLoreFunc(ObjectList<String> lines, InsertionMode insertionMode) implements ILootModifierFunc {

    @Override
    public void modify(VersionHandler versionHandler, Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        if (metaRef.isEmpty()) {
            return;
        }
        ObjectArrayList<String> newLore = lines.stream().map(line -> ComponentBuilder.parse(line).asLegacyText())
            .collect(ObjectArrayList.toList());
        if (insertionMode == InsertionMode.SET) {
            metaRef.get().setLore(newLore);
            return;
        }
        ItemMeta meta = metaRef.get();
        if (meta.hasLore()) {
            if (insertionMode == InsertionMode.PREPEND) {
                newLore.addAll(meta.getLore());
            } else {
                newLore.addAll(0, meta.getLore());
            }
        }
        meta.setLore(newLore);
    }

}
