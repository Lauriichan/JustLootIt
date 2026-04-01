package me.lauriichan.spigot.justlootit.loot.filter;

import java.util.Random;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.lauriichan.spigot.justlootit.loot.ILootFilter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;

public record NameRegexFilter(String stringPattern, Predicate<String> predicate, boolean withColors) implements ILootFilter {

    public NameRegexFilter(String stringPattern, boolean withColors) {
        this(stringPattern, Pattern.compile(stringPattern).asMatchPredicate(), withColors);
    }

    @Override
    public boolean includes(VersionHandler versionHandler, Random random, ItemStack itemStack) {
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (!itemMeta.hasDisplayName()) {
            return false;
        }
        String displayName = itemMeta.getDisplayName();
        if (!withColors) {
            displayName = ChatColor.stripColor(displayName);
        }
        return predicate.test(displayName);
    }

}
