package me.lauriichan.spigot.justlootit.command.argument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.loot.LootTable;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentMap;
import me.lauriichan.laylib.command.IArgumentType;
import me.lauriichan.laylib.command.Suggestions;
import me.lauriichan.spigot.justlootit.command.impl.LootItActor;
import me.lauriichan.spigot.justlootit.util.ImprovedLevenshteinDistance;

public final class LootTableArgument implements IArgumentType<LootTable> {

    @Override
    public LootTable parse(Actor<?> actor, String input, IArgumentMap map) throws IllegalArgumentException {
        NamespacedKey key = NamespacedKey.fromString(input);
        if (key == null) {
            throw new IllegalArgumentException("Key '" + input + "' is invalid!");
        }
        LootTable table = actor instanceof LootItActor<?> bukkit ? bukkit.versionHelper().getLootTable(key) : Bukkit.getLootTable(key);
        if (table == null) {
            throw new IllegalArgumentException("Unknown loot table '" + input + "'!");
        }
        return table;
    }

    @Override
    public void suggest(Actor<?> actor, String input, Suggestions suggestions, IArgumentMap map) {
        if (!(actor instanceof LootItActor<?> bukkit)) {
            return;
        }
        String namespace;
        String key;
        if((input = input.toLowerCase()).contains(":")) {
            String[] split = input.split(":", 2);
            namespace = split[0];
            key = split[1];
        } else {
            namespace = "minecraft";
            key = input;
        }
        ArrayList<String> collectionList = new ArrayList<>();
        bukkit.versionHelper().getLootTables().forEach(namespacedKey -> {
            if(namespacedKey.getKey().equals("empty")) {
                return;
            }
            collectionList.add(namespacedKey.toString());
        });
        List<String> prefixList = collectionList.stream().filter(string -> string.startsWith(namespace)).toList();
        if(prefixList.isEmpty()) {
            prefixList = collectionList;
        }
        List<String> rankingList = prefixList.stream().filter(string -> string.contains(key)).toList();
        if(rankingList.isEmpty()) {
            rankingList = collectionList;
        }
        List<Entry<String, Integer>> list = ImprovedLevenshteinDistance.rankByDistance(namespace + ':' + key, collectionList);
        double max = list.stream().map(Entry::getValue).collect(Collectors.summingInt(Integer::intValue));
        for (int index = 0; index < list.size(); index++) {
            Entry<String, Integer> entry = list.get(index);
            suggestions.suggest(1 - (entry.getValue().doubleValue() / max), entry.getKey());
        }
    }

}
