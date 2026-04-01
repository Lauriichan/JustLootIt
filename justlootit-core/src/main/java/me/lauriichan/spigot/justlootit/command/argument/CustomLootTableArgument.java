package me.lauriichan.spigot.justlootit.command.argument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.NamespacedKey;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentMap;
import me.lauriichan.laylib.command.IArgumentType;
import me.lauriichan.laylib.command.Suggestions;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.data.CustomLootTable;
import me.lauriichan.spigot.justlootit.config.loot.LootTableDirectoryData;
import me.lauriichan.spigot.justlootit.util.ImprovedLevenshteinDistance;

public final class CustomLootTableArgument implements IArgumentType<CustomLootTable> {

    public static boolean isLootTable(JustLootItPlugin plugin, String input) {
        NamespacedKey key = NamespacedKey.fromString(input, plugin);
        if (key == null) {
            return false;
        }
        CustomLootTable table = plugin.dataManager().directoryData(LootTableDirectoryData.class).get(key);
        return table != null;
    }

    public static CustomLootTable parseLootTable(JustLootItPlugin plugin, String input) throws IllegalArgumentException {
        NamespacedKey key = NamespacedKey.fromString(input, plugin);
        if (key == null) {
            throw new IllegalArgumentException("Key '" + input + "' is invalid!");
        }
        CustomLootTable table = plugin.dataManager().directoryData(LootTableDirectoryData.class).get(key);
        if (table == null) {
            throw new IllegalArgumentException("Unknown loot table '" + input + "'!");
        }
        return table;
    }

    private final JustLootItPlugin plugin = JustLootItPlugin.get();

    @Override
    public CustomLootTable parse(Actor<?> actor, String input, IArgumentMap map) throws IllegalArgumentException {
        return parseLootTable(plugin, input);
    }

    @Override
    public void suggest(Actor<?> actor, String input, Suggestions suggestions, IArgumentMap map) {
        String namespace;
        String key;
        if ((input = input.toLowerCase()).contains(":")) {
            String[] split = input.split(":", 2);
            namespace = split[0];
            key = split[1];
        } else {
            namespace = "justlootit";
            key = input;
        }
        ArrayList<String> collectionList = new ArrayList<>();
        plugin.dataManager().directoryData(LootTableDirectoryData.class).getTables()
            .forEach(table -> collectionList.add(table.id().toString()));
        List<String> prefixList = collectionList.stream().filter(string -> string.startsWith(namespace)).toList();
        if (prefixList.isEmpty()) {
            prefixList = collectionList;
        }
        List<String> rankingList = prefixList.stream().filter(string -> string.contains(key)).toList();
        if (rankingList.isEmpty()) {
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
