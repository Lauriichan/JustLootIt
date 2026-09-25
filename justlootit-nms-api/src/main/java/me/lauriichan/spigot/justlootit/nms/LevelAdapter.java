package me.lauriichan.spigot.justlootit.nms;

import java.io.File;

import org.bukkit.GameEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.lauriichan.spigot.justlootit.nms.capability.Capable;

public abstract class LevelAdapter extends Capable<LevelAdapter> {

    public abstract VersionHandler versionHandler();

    public abstract World asBukkit();
    
    public abstract File dataFolder();

    public abstract Entity getBukkitEntityById(int id);
    
    public String determineDimensionType() {
        return null;
    }
    
    public abstract void triggerGameEvent(Player player, GameEvent event, Location location);
    
    public abstract void triggerBlockOpen(Player player, Location location);
    
    public abstract void triggerBlockClose(Player player, Location location);

}
