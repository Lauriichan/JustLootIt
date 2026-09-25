package me.lauriichan.spigot.justlootit.nms;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;

public interface IMinecraftRandomFactory {

    default IMinecraftRandom at(Location location) {
        return at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    IMinecraftRandom at(int x, int y, int z);

    default IMinecraftRandom fromHashOf(NamespacedKey key) {
        return fromHashOf(key.toString());
    }

    IMinecraftRandom fromHashOf(String name);

    IMinecraftRandom fromSeed(long seed);

}
