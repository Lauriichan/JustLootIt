package me.lauriichan.spigot.justlootit.nms.v1_19_R2;

import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class LevelAdapter1_19_R2 extends LevelAdapter {

    private final ServerLevel level;
    private final VersionHandler1_19_R2 versionHandler;

    public LevelAdapter1_19_R2(final VersionHandler1_19_R2 versionHandler, final ServerLevel level) {
        this.level = level;
        this.versionHandler = versionHandler;
    }

    @Override
    public VersionHandler1_19_R2 versionHandler() {
        return versionHandler;
    }

    @Override
    public CraftWorld asBukkit() {
        return level.getWorld();
    }

    @Override
    public org.bukkit.entity.Entity getBukkitEntityById(final int id) {
        final Entity entity = level.entityManager.getEntityGetter().get(id);
        if (entity == null) {
            return null;
        }
        return entity.getBukkitEntity();
    }

}
