package me.lauriichan.spigot.justlootit.nms;

import org.bukkit.World;
import org.bukkit.entity.Entity;

public abstract class LevelAdapter {

    public abstract World asBukkit();

    public abstract Entity getBukkitEntityById(int id);

}
