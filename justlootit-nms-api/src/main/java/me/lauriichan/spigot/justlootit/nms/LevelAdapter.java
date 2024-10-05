package me.lauriichan.spigot.justlootit.nms;

import org.bukkit.GameEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.lauriichan.spigot.justlootit.nms.capability.Capable;

public abstract class LevelAdapter extends Capable<LevelAdapter> {

    public abstract VersionHandler versionHandler();

    public abstract World asBukkit();

    public abstract Entity getBukkitEntityById(int id);
    
    public abstract void triggerGameEvent(Player player, GameEvent event, Location location);

}
