package me.lauriichan.spigot.justlootit.nms.v1_21_R2;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R2.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.v1_21_R2.util.PlatformHelper1_21_R2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.LevelEntityGetter;

public class LevelAdapter1_21_R2 extends LevelAdapter {
    
    private final VersionHandler1_21_R2 versionHandler;

    private final ServerLevel level;
    private final LevelEntityGetter<Entity> entityGetter;

    public LevelAdapter1_21_R2(final VersionHandler1_21_R2 versionHandler, final ServerLevel level) {
        this.versionHandler = versionHandler;
        this.level = level;
        this.entityGetter = PlatformHelper1_21_R2.getEntityGetter(level);
    }

    @Override
    public VersionHandler1_21_R2 versionHandler() {
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
        level.gameEvent(((CraftPlayer) player).getHandle(), CraftRegistry.bukkitToMinecraftHolder(event, Registries.GAME_EVENT),
            new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

}