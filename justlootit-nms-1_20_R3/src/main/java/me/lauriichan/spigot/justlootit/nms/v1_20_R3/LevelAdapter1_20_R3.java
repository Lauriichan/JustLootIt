package me.lauriichan.spigot.justlootit.nms.v1_20_R3;

import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class LevelAdapter1_20_R3 extends LevelAdapter {

    private final ServerLevel level;
    private final VersionHandler1_20_R3 versionHandler;

    public LevelAdapter1_20_R3(final VersionHandler1_20_R3 versionHandler, final ServerLevel level) {
        this.level = level;
        this.versionHandler = versionHandler;
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
        final Entity entity = level.entityManager.getEntityGetter().get(id);
        if (entity == null) {
            return null;
        }
        return entity.getBukkitEntity();
    }

}