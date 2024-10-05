package me.lauriichan.spigot.justlootit.nms.v1_20_R3;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.CraftGameEvent;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.v1_20_R3.util.PlatformHelper1_20_R3;
import net.minecraft.core.BlockPos;
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

    @Override
    public void triggerGameEvent(Player player, org.bukkit.GameEvent event, Location location) {
        level.gameEvent(((CraftPlayer) player).getHandle(), ((CraftGameEvent) event).getHandle(),
            new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

}