package me.lauriichan.spigot.justlootit.nms.paper.v26_1;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

public class LevelAdapter26_1 extends LevelAdapter {

    private final VersionHandler26_1 versionHandler;

    private final ServerLevel level;
    private final LevelEntityGetter<Entity> entityGetter;

    public LevelAdapter26_1(final VersionHandler26_1 versionHandler, final ServerLevel level) {
        this.versionHandler = versionHandler;
        this.level = level;
        this.entityGetter = level.moonrise$getEntityLookup();
    }

    @Override
    public VersionHandler26_1 versionHandler() {
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

    @Override
    public void triggerGameEvent(Player player, org.bukkit.GameEvent event, Location location) {
        level.gameEvent(((CraftPlayer) player).getHandle(), CraftRegistry.bukkitToMinecraftHolder(event),
            new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

}