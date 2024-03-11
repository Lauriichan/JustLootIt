package me.lauriichan.spigot.justlootit.nms.v1_20_R3;

import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.v1_20_R3.util.PlatformHelper1_20_R3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

public class LevelAdapter1_20_R3 extends LevelAdapter {
    
    private final VersionHandler1_20_R3 versionHandler;

    private final ServerLevel level;
    private final LevelEntityGetter<Entity> entityGetter;

    public LevelAdapter1_20_R3(final VersionHandler1_20_R3 versionHandler, final ServerLevel level) {
        this.versionHandler = versionHandler;
        this.level = level;
        this.entityGetter = PlatformHelper1_20_R3.getEntityGetter(level);
    }

    @Override
    public VersionHandler1_20_R3 versionHandler() {
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