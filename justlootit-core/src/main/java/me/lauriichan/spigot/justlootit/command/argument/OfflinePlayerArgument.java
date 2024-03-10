package me.lauriichan.spigot.justlootit.command.argument;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentMap;
import me.lauriichan.laylib.command.IArgumentType;
import me.lauriichan.laylib.command.Suggestions;
import me.lauriichan.minecraft.pluginbase.command.argument.UUIDArgument;
import me.lauriichan.spigot.justlootit.util.ImprovedLevenshteinDistance;
import me.lauriichan.spigot.justlootit.util.MojangServer;

public class OfflinePlayerArgument implements IArgumentType<OfflinePlayer> {

    @Override
    public OfflinePlayer parse(Actor<?> actor, String input, IArgumentMap map) throws IllegalArgumentException {
        if (input.length() < 3) {
            throw new IllegalArgumentException("Unknown player '%s'".formatted(input));
        }
        Player player = Bukkit.getPlayerExact(input);
        if (player != null) {
            return player;
        }
        if (input.length() <= 16) {
            UUID uuid = MojangServer.getUniqueId(input).join();
            if (uuid == null) {
                throw new IllegalArgumentException("Unknown player '%s'".formatted(input));
            }
            return Bukkit.getOfflinePlayer(uuid);
        }
        return Bukkit.getOfflinePlayer(UUIDArgument.uuidFromString(input));
    }

    @Override
    public void suggest(Actor<?> actor, String input, Suggestions suggestions, IArgumentMap map) {
        if (input.length() > 16) {
            return;
        }
        for (Entry<String, Integer> entry : ImprovedLevenshteinDistance.rankByDistance(input,
            Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).toArray(String[]::new))) {
            suggestions.suggest(entry.getValue(), entry.getKey());
        }
    }

}
