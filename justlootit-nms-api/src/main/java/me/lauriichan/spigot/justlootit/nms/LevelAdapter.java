package me.lauriichan.spigot.justlootit.nms;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import me.lauriichan.spigot.justlootit.nms.capability.Capable;

public abstract class LevelAdapter extends Capable<LevelAdapter> {
    
    public abstract VersionHandler versionHandler();

    public abstract World asBukkit();

    public abstract Entity getBukkitEntityById(int id);

}
