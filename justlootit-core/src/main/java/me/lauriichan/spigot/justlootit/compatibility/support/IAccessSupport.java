package me.lauriichan.spigot.justlootit.compatibility.support;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface IAccessSupport {

    @SupportDefault(name = "DEFAULT_TRUE")
    boolean canAccess(Player player, Location location);

}
