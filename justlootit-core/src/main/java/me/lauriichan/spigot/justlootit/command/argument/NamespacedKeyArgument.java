package me.lauriichan.spigot.justlootit.command.argument;

import org.bukkit.NamespacedKey;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.laylib.command.IArgumentMap;
import me.lauriichan.laylib.command.IArgumentType;

public final class NamespacedKeyArgument implements IArgumentType<NamespacedKey> {

    @Override
    public NamespacedKey parse(Actor<?> actor, String input, IArgumentMap map) throws IllegalArgumentException {
        NamespacedKey key = NamespacedKey.fromString(input);
        if (key == null) {
            throw new IllegalArgumentException("Key '" + input + "' is invalid!");
        }
        return key;
    }

}
