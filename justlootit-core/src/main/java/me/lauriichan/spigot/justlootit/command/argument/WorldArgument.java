package me.lauriichan.spigot.justlootit.command.argument;

import org.bukkit.Bukkit;
import org.bukkit.World;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentMap;
import me.lauriichan.laylib.command.IArgumentType;
import me.lauriichan.laylib.command.Suggestions;

public class WorldArgument implements IArgumentType<World> {

    @Override
    public World parse(Actor<?> actor, String input, IArgumentMap map) throws IllegalArgumentException {
        World world = Bukkit.getWorld(input);
        if (world == null) {
            throw new IllegalArgumentException("Unknown world '" + input + "'");
        }
        return world;
    }

    @Override
    public void suggest(Actor<?> actor, String input, Suggestions suggestions, IArgumentMap map) {
        int length = input.length();
        for (World world : Bukkit.getWorlds()) {
            String name = world.getName();
            if (!name.contains(input)) {
                continue;
            }
            suggestions.suggest(length - name.length(), name);
        }
    }

}
