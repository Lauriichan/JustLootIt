package me.lauriichan.spigot.justlootit.loot.filter;

import java.util.Random;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import me.lauriichan.spigot.justlootit.loot.ILootFilter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record AndFilter(ObjectList<ILootFilter> filters) implements ILootFilter {

    public AndFilter(ObjectList<ILootFilter> filters) {
        this.filters = ObjectLists.unmodifiable(filters);
    }

    @Override
    public boolean includes(VersionHandler versionHandler, Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        for (ILootFilter filter : filters) {
            if (!filter.includes(versionHandler, random, itemRef, metaRef)) {
                return false;
            }
        }
        return true;
    }

}
