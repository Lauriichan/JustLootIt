package me.lauriichan.spigot.justlootit.command.argument;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentType;
import me.lauriichan.laylib.command.Suggestions;
import me.lauriichan.laylib.command.util.LevenshteinDistance;
import me.lauriichan.spigot.justlootit.command.impl.BukkitActor;

public final class LootTableArgument implements IArgumentType<LootTable> {

    @Override
    public LootTable parse(Actor<?> actor, String input) throws IllegalArgumentException {
        NamespacedKey key = NamespacedKey.fromString(input);
        if (key == null) {
            throw new IllegalArgumentException("Key '" + input + "' is invalid!");
        }
        LootTable table = actor instanceof BukkitActor<?> bukkit ? bukkit.versionHelper().getLootTable(key) : Bukkit.getLootTable(key);
        if (table == null) {
            throw new IllegalArgumentException("Unknown loot table '" + input + "'!");
        }
        return table;
    }

    @Override
    public void suggest(Actor<?> actor, String input, Suggestions suggestions) {
        if (!(actor instanceof BukkitActor<?> bukkit)) {
            return;
        }
        List<Entry<String, Integer>> list = LevenshteinDistance.rankByDistance(input,
            bukkit.versionHelper().getLootTables().stream().map(NamespacedKey::toString).toList());
        double max = list.stream().map(Entry::getValue).collect(Collectors.summingInt(Integer::intValue));
        for (int index = 0; index < list.size(); index++) {
            Entry<String, Integer> entry = list.get(index);
            suggestions.suggest(1 - (entry.getValue().doubleValue() / max), entry.getKey());
        }
    }

}
