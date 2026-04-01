package me.lauriichan.spigot.justlootit.loot.filter;

import java.util.Random;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.loot.ILootFilter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.util.Ref;

public record LoreRegexFilter(String stringPattern, Predicate<String> predicate, boolean withColors) implements ILootFilter {

    public LoreRegexFilter(String stringPattern, boolean withColors) {
        this(stringPattern, Pattern.compile(stringPattern).asMatchPredicate(), withColors);
    }

    @Override
    public boolean includes(VersionHandler versionHandler, Random random, Ref<ItemStack> itemRef, Ref<ItemMeta> metaRef) {
        ItemMeta itemMeta = metaRef.get();
        if (!itemMeta.hasLore()) {
            return false;
        }
        Stream<String> stream = itemMeta.getLore().stream();
        if (!withColors) {
            stream.map(ChatColor::stripColor);
        }
        return stream.anyMatch(predicate);
    }

}
