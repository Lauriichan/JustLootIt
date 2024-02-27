package me.lauriichan.spigot.justlootit.command.argument;

import java.util.Map.Entry;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentMap;
import me.lauriichan.laylib.command.IArgumentType;
import me.lauriichan.laylib.command.Suggestions;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.config.RefreshConfig;
import me.lauriichan.spigot.justlootit.config.data.RefreshGroup;
import me.lauriichan.spigot.justlootit.util.ImprovedLevenshteinDistance;

public final class RefreshGroupArgument implements IArgumentType<RefreshGroup> {
    
    private final RefreshConfig config = JustLootItPlugin.get().configManager().config(RefreshConfig.class);

    @Override
    public RefreshGroup parse(Actor<?> actor, String input, IArgumentMap map) throws IllegalArgumentException {
        RefreshGroup group = config.group(input);
        if (group == null) {
            throw new IllegalArgumentException("Unknown group: '" + input + "'");
        }
        return group;
    }

    @Override
    public void suggest(Actor<?> actor, String input, Suggestions suggestions, IArgumentMap map) {
        for (Entry<String, Integer> entry : ImprovedLevenshteinDistance.rankByDistance(input, config.ids())) {
            suggestions.suggest(entry.getValue(), entry.getKey());
        }
    }

}
