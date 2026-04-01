package me.lauriichan.spigot.justlootit.loot.filter;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import me.lauriichan.spigot.justlootit.loot.ILootFilter;
import me.lauriichan.spigot.justlootit.loot.filter.tag.CompoundTagFilter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.nbt.TagType;

public record CustomDataFilter(CompoundTagFilter filter) implements ILootFilter {

    @Override
    public boolean includes(VersionHandler versionHandler, Random random, ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        return filter.matches(TagType.COMPOUND, versionHandler.nbtHelper().getCustomDataTag(itemStack));
    }

}
