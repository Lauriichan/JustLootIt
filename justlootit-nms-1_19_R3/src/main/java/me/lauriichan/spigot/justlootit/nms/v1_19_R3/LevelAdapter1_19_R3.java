package me.lauriichan.spigot.justlootit.nms.v1_19_R3;

import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class LevelAdapter1_19_R3 extends LevelAdapter {
    
    private final ServerLevel level;
    
    public LevelAdapter1_19_R3(final ServerLevel level) {
        this.level = level;
    }

    @Override
    public CraftWorld asBukkit() {
        return level.getWorld();
    }

    @Override
    public org.bukkit.entity.Entity getBukkitEntityById(int id) {
        Entity entity = level.entityManager.getEntityGetter().get(id);
        if(entity == null) {
            return null;
        }
        return entity.getBukkitEntity();
    }

}