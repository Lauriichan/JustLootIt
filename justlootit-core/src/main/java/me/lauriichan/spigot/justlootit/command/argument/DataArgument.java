package me.lauriichan.spigot.justlootit.command.argument;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentMap;
import me.lauriichan.laylib.command.IArgumentType;
import me.lauriichan.laylib.command.Suggestions;
import me.lauriichan.minecraft.pluginbase.data.DataManager;
import me.lauriichan.minecraft.pluginbase.data.IDataWrapper;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.util.ImprovedLevenshteinDistance;
import me.lauriichan.spigot.justlootit.util.TypeName;

public final class DataArgument implements IArgumentType<IDataWrapper<?, ?>> {

    private final DataManager dataManager = JustLootItPlugin.get().dataManager();

    @Override
    public IDataWrapper<?, ?> parse(final Actor<?> actor, String input, final IArgumentMap map) throws IllegalArgumentException {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("No valid config name specified.");
        }
        input = input.toLowerCase();
        for (final IDataWrapper<?, ?> wrapper : dataManager.wrappers()) {
            if (input.equals(TypeName.ofData(wrapper))) {
                return wrapper;
            }
        }
        throw new IllegalArgumentException("Unknown config '" + input + "'!");
    }

    @Override
    public void suggest(final Actor<?> actor, final String input, final Suggestions suggestions, final IArgumentMap map) {
        final ObjectArrayList<String> configs = new ObjectArrayList<>();
        for (final IDataWrapper<?, ?> wrapper : dataManager.wrappers()) {
            configs.add(TypeName.ofData(wrapper));
        }
        for (final Map.Entry<String, Integer> entry : ImprovedLevenshteinDistance.rankByDistance(input, configs)) {
            suggestions.suggest(entry.getValue(), entry.getKey());
        }
    }

}
