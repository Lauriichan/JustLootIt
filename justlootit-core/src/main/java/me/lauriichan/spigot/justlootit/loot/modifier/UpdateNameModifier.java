package me.lauriichan.spigot.justlootit.loot.modifier;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.minecraft.pluginbase.message.component.ComponentBuilder;
import me.lauriichan.spigot.justlootit.loot.ILootModifier;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record UpdateNameModifier(String newNameText, InsertionMode insertionMode) implements ILootModifier {

    @Override
    public void modify(VersionHandler versionHandler, Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        if (metaRef.isEmpty()) {
            return;
        }
        String newName = ComponentBuilder.parse(newNameText).asLegacyText();
        if (insertionMode == InsertionMode.SET) {
            metaRef.get().setDisplayName(newName);
            return;
        }
        ItemMeta meta = metaRef.get();
        if (meta.hasLore()) {
            if (insertionMode == InsertionMode.PREPEND) {
                newName = newName + meta.getDisplayName();
            } else {
                newName = meta.getDisplayName() + newName;
            }
        }
        meta.setDisplayName(newName);
    }

}
