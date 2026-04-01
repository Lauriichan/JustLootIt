package me.lauriichan.spigot.justlootit.loot.condition;

import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.loot.ILootCondition;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public record LootTableRegexCondition(String stringPattern, Predicate<String> predicate) implements ILootCondition {
    
    @Override
    public boolean includes(Container container, PlayerAdapter player, Location location, NamespacedKey lootTableKey) {
        return lootTableKey != null && predicate.test(lootTableKey.toString());
    }

}