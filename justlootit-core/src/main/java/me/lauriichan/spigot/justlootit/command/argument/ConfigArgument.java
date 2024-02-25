package me.lauriichan.spigot.justlootit.command.argument;

import java.util.ArrayList;
import java.util.Map.Entry;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentMap;
import me.lauriichan.laylib.command.IArgumentType;
import me.lauriichan.laylib.command.Suggestions;
import me.lauriichan.minecraft.pluginbase.config.ConfigManager;
import me.lauriichan.minecraft.pluginbase.config.ConfigWrapper;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.util.ImprovedLevenshteinDistance;
import me.lauriichan.spigot.justlootit.util.TypeName;

public class ConfigArgument implements IArgumentType<ConfigWrapper<?>> {

    @Override
    public ConfigWrapper<?> parse(Actor<?> actor, String input, IArgumentMap map) throws IllegalArgumentException {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("No valid config name specified.");
        }
        input = input.toLowerCase();
        ConfigManager configManager = JustLootItPlugin.get().configManager();
        for (ConfigWrapper<?> wrapper : configManager.wrappers()) {
            if (input.equals(TypeName.ofConfig(wrapper.config()))) {
                return wrapper;
            }
        }
        throw new IllegalArgumentException("Unknown config '" + input + "'!");
    }

    @Override
    public void suggest(Actor<?> actor, String input, Suggestions suggestions, IArgumentMap map) {
        ConfigManager configManager = JustLootItPlugin.get().configManager();
        ArrayList<String> configs = new ArrayList<>();
        for (ConfigWrapper<?> wrapper : configManager.wrappers()) {
            configs.add(TypeName.ofConfig(wrapper.config()));
        }
        for (Entry<String, Integer> entry : ImprovedLevenshteinDistance.rankByDistance(input, configs)) {
            suggestions.suggest(entry.getValue(), entry.getKey());
        }
    }

}
