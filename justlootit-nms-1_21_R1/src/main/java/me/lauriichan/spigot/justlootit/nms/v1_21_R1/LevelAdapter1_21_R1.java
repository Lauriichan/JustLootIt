package me.lauriichan.spigot.justlootit.nms.v1_21_R1;

import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.v1_21_R1.util.PlatformHelper1_21_R1;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

public class LevelAdapter1_21_R1 extends LevelAdapter {
    
    private final VersionHandler1_21_R1 versionHandler;

    private final ServerLevel level;
    private final LevelEntityGetter<Entity> entityGetter;

    public LevelAdapter1_21_R1(final VersionHandler1_21_R1 versionHandler, final ServerLevel level) {
        this.versionHandler = versionHandler;
        this.level = level;
        this.entityGetter = PlatformHelper1_21_R1.getEntityGetter(level);
    }

    @Override
    public VersionHandler1_21_R1 versionHandler() {
        return versionHandler;
    }

    @Override
    public CraftWorld asBukkit() {
        return level.getWorld();
    }

    @Override
    public org.bukkit.entity.Entity getBukkitEntityById(final int id) {
        final Entity entity = entityGetter.get(id);
        if (entity == null) {
            return null;
        }
        return entity.getBukkitEntity();
    }

}