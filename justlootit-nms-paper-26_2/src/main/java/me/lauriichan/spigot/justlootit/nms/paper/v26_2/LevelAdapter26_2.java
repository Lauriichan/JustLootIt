package me.lauriichan.spigot.justlootit.nms.paper.v26_2;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.entity.LevelEntityGetter;

public class LevelAdapter26_2 extends LevelAdapter {

    private final VersionHandler26_2 versionHandler;

    private final ServerLevel level;
    private final LevelEntityGetter<Entity> entityGetter;

    public LevelAdapter26_2(final VersionHandler26_2 versionHandler, final ServerLevel level) {
        this.versionHandler = versionHandler;
        this.level = level;
        this.entityGetter = level.moonrise$getEntityLookup();
    }

    @Override
    public VersionHandler26_2 versionHandler() {
        return versionHandler;
    }

    @Override
    public CraftWorld asBukkit() {
        return level.getWorld();
    }
    
    @Override
    public File dataFolder() {
        return new File(level.getWorld().getWorldFolder(), "data");
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
    public String determineDimensionType() {
        return level.dimensionTypeRegistration().unwrapKey().map(ResourceKey::identifier).map(Identifier::toString).orElse(null);
    }

    @Override
    public void triggerGameEvent(Player player, org.bukkit.GameEvent event, Location location) {
        level.gameEvent(((CraftPlayer) player).getHandle(), CraftRegistry.bukkitToMinecraftHolder(event),
            new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    @Override
    public void triggerBlockOpen(Player player, Location location) {
        BlockEntity blockEntity = level.getBlockEntity(new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        if (blockEntity == null) {
            return;
        }
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        if (blockEntity instanceof BarrelBlockEntity bbe) {
            bbe.startOpen(serverPlayer);
        } else if (blockEntity instanceof ChestBlockEntity cbe) {
            cbe.startOpen(serverPlayer);
        } else if (blockEntity instanceof EnderChestBlockEntity ecbe) {
            ecbe.startOpen(serverPlayer);
        } else if (blockEntity instanceof ShulkerBoxBlockEntity sbbe) {
            sbbe.startOpen(serverPlayer);
        }
    }

    @Override
    public void triggerBlockClose(Player player, Location location) {
        BlockEntity blockEntity = level.getBlockEntity(new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        if (blockEntity == null) {
            return;
        }
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        if (blockEntity instanceof BarrelBlockEntity bbe) {
            bbe.stopOpen(serverPlayer);
        } else if (blockEntity instanceof ChestBlockEntity cbe) {
            cbe.stopOpen(serverPlayer);
        } else if (blockEntity instanceof EnderChestBlockEntity ecbe) {
            ecbe.stopOpen(serverPlayer);
        } else if (blockEntity instanceof ShulkerBoxBlockEntity sbbe) {
            sbbe.stopOpen(serverPlayer);
        }
    }

}