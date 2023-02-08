package me.lauriichan.spigot.justlootit.nms.v1_19_R2;

import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class LevelAdapter1_19_R2 extends LevelAdapter {
    
    private final ServerLevel level;
    
    public LevelAdapter1_19_R2(final ServerLevel level) {
        this.level = level;
    }

    @Override
    public CraftWorld asBukkit() {
        return level.getWorld();
    }

    @Override
    public org.bukkit.entity.Entity getBukkitEntityById(int id) {
        Entity entity = level.getEntity(id);
        if(entity == null) {
            return null;
        }
        return entity.getBukkitEntity();
    }

}
