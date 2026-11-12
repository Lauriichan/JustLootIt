package me.lauriichan.spigot.justlootit.compatibility.support;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IDestructionSupport {
    
    @SupportDefault(name = "DEFAULT_TRUE")
    boolean canDestroy(Player player, Location location);

}
