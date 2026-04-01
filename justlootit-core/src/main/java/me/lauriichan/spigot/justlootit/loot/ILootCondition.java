package me.lauriichan.spigot.justlootit.loot;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;

import me.lauriichan.spigot.justlootit.data.Container;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public interface ILootCondition {
    
    boolean includes(Container container, PlayerAdapter player, Location location, NamespacedKey lootTableKey);

}
