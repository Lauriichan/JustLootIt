package me.lauriichan.spigot.justlootit.loot.modify.filter;

import java.util.Random;

import org.bukkit.inventory.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.spigot.justlootit.loot.modify.ILootFilter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public record OrFilter(ObjectList<ILootFilter> filters) implements ILootFilter {

    public OrFilter(ObjectList<ILootFilter> filters) {
        this.filters = ObjectLists.unmodifiable(filters);
    }

    @Override
    public boolean includes(VersionHandler versionHandler, Random random, ItemStack itemStack) {
        for (ILootFilter filter : filters) {
            if (filter.includes(versionHandler, random, itemStack)) {
                return true;
            }
        }
        return false;
    }

}
