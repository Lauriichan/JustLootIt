package me.lauriichan.spigot.justlootit.loot.filter;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.loot.ILootFilter;
import me.lauriichan.spigot.justlootit.loot.filter.tag.CompoundTagFilter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record CustomDataFilter(CompoundTagFilter filter) implements ILootFilter {

    @Override
    public boolean includes(VersionHandler versionHandler, Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        ItemStack cloned = itemRef.get().clone();
        cloned.setItemMeta(metaRef.get().clone());
        return filter.matches(TagType.COMPOUND, versionHandler.nbtHelper().getCustomDataTag(cloned));
    }

}
