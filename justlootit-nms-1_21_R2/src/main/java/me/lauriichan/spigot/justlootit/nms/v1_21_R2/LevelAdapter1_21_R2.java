package me.lauriichan.spigot.justlootit.nms.v1_21_R2;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R2.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R2.event.CraftEventFactory;
import org.bukkit.entity.Player;

import me.lauriichan.spigot.justlootit.nms.IMinecraftRandom;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
import me.lauriichan.spigot.justlootit.nms.v1_21_R2.util.NmsHelper1_21_R2;
import me.lauriichan.spigot.justlootit.nms.v1_21_R2.util.PlatformHelper1_21_R2;
import me.lauriichan.spigot.justlootit.nms.v1_21_R2.util.random.MinecraftRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
    public IMinecraftRandom random() {
        return new MinecraftRandom(level.getRandom());
    }

    @Override
    public void triggerGameEvent(Player player, org.bukkit.GameEvent event, Location location) {
        level.gameEvent(((CraftPlayer) player).getHandle(), CraftRegistry.bukkitToMinecraftHolder(event, Registries.GAME_EVENT),
            new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }

    @Override
    public void triggerBlockOpen(Player player, Location location) {
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity == null) {
            return;
        }
        BlockState blockState = blockEntity.getBlockState();
        ContainerOpenersCounter counter;
        boolean isTrapped = false;
        if (blockEntity instanceof BarrelBlockEntity bbe) {
            counter = bbe.openersCounter;
        } else if (blockEntity instanceof ChestBlockEntity cbe) {
            counter = cbe.openersCounter;
            isTrapped = cbe instanceof TrappedChestBlockEntity;
        } else if (blockEntity instanceof EnderChestBlockEntity ecbe) {
            counter = ecbe.openersCounter;
        } else if (blockEntity instanceof ShulkerBoxBlockEntity sbbe) {
            int openCount = sbbe.openCount, newCount = Math.max(openCount + 1, 1);
            level.blockEvent(blockPos, blockState.getBlock(), openCount, sbbe.openCount = newCount);
            if (!sbbe.opened && newCount > 0) {
                sbbe.opened = true;
            }
            return;
        } else {
            return;
        }
        int openerCount = counter.getOpenerCount(), newCount = Math.max(openerCount + 1, 1);
        if (isTrapped) {
            int oldPower = Math.max(0, Math.min(15, openerCount));
            int newPower = Math.max(0, Math.min(15, newCount));
            if (oldPower != newPower) {
                CraftEventFactory.callRedstoneChange(level, blockPos, oldPower, newPower);
            }
        }
        counter.onAPIOpen(level, blockPos, blockState);
        NmsHelper1_21_R2.setOpenCount(counter, newCount);
        counter.openerAPICountChanged(level, blockPos, blockState, openerCount, newCount);
        if (!counter.opened && newCount > 0) {
            counter.opened = true;
        }
    }

    @Override
    public void triggerBlockClose(Player player, Location location) {
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity == null) {
            return;
        }
        BlockState blockState = blockEntity.getBlockState();
        ContainerOpenersCounter counter;
        boolean isTrapped = false;
        if (blockEntity instanceof BarrelBlockEntity bbe) {
            counter = bbe.openersCounter;
        } else if (blockEntity instanceof ChestBlockEntity cbe) {
            counter = cbe.openersCounter;
            isTrapped = cbe instanceof TrappedChestBlockEntity;
        } else if (blockEntity instanceof EnderChestBlockEntity ecbe) {
            counter = ecbe.openersCounter;
        } else if (blockEntity instanceof ShulkerBoxBlockEntity sbbe) {
            int openCount = sbbe.openCount, newCount = Math.max(openCount - 1, 0);
            level.blockEvent(blockPos, blockState.getBlock(), openCount, sbbe.openCount = newCount);
            if (sbbe.opened && newCount == 0) {
                sbbe.opened = false;
            }
            return;
        } else {
            return;
        }
        int openerCount = counter.getOpenerCount(), newCount = Math.max(openerCount - 1, 0);
        if (isTrapped) {
            int oldPower = Math.max(0, Math.min(15, openerCount));
            int newPower = Math.max(0, Math.min(15, newCount));
            if (oldPower != newPower) {
                CraftEventFactory.callRedstoneChange(level, blockPos, oldPower, newPower);
            }
        }
        counter.onAPIClose(level, blockPos, blockState);
        NmsHelper1_21_R2.setOpenCount(counter, newCount);
        counter.openerAPICountChanged(level, blockPos, blockState, openerCount, newCount);
        if (counter.opened && newCount == 0) {
            counter.opened = false;
        }
    }

}