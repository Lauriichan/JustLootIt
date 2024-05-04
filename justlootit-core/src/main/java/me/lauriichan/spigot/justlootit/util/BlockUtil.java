package me.lauriichan.spigot.justlootit.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.RegionAccessor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Player;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.nms.convert.ProtoChunk;
import me.lauriichan.spigot.justlootit.nms.util.Vec3i;
import me.lauriichan.spigot.justlootit.platform.scheduler.Scheduler;

public final class BlockUtil {

    private BlockUtil() {
        throw new UnsupportedOperationException();
    }

    public static Container findChestAround(RegionAccessor region, Location location, Type chestType, BlockFace chestFace) {
        return findChestAround(region, location.getBlockX(), location.getBlockY(), location.getBlockZ(), chestType, chestFace);
    }

    public static Vec3i findChestLocationAround(Vec3i location, Type chestType, BlockFace chestFace) {
        if (chestFace.getModZ() != 0) {
            location.addX(chestType == Type.LEFT ? -chestFace.getModZ() : chestFace.getModZ());
        } else {
            location.addZ(chestType == Type.LEFT ? chestFace.getModX() : -chestFace.getModX());
        }
        return location;
    }

    public static Container findChestAround(RegionAccessor region, int x, int y, int z, Type chestType, BlockFace chestFace) {
        if (chestFace.getModZ() != 0) {
            x += chestType == Type.LEFT ? -chestFace.getModZ() : chestFace.getModZ();
        } else {
            z += chestType == Type.LEFT ? chestFace.getModX() : -chestFace.getModX();
        }
        BlockState state = region.getBlockState(x, y, z);
        if (state instanceof Container container) {
            return container;
        }
        return null;
    }

    public static void sendBlockOpen(Player player, Location location) {
        BlockState state = player.getWorld().getBlockState(location);
        Material type = state.getType();
        if (type == Material.BARREL) {
            player.playSound(location, Sound.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        } else if (type == Material.CHEST) {
            player.playSound(location, Sound.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        } else if (type == Material.ENDER_CHEST) {
            player.playSound(location, Sound.BLOCK_ENDER_CHEST_OPEN, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        } else if (state instanceof ShulkerBox shulker) {
            player.playSound(location, Sound.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        }
    }

    public static void sendBlockClose(Player player, Location location) {
        BlockState state = player.getWorld().getBlockState(location);
        Material type = state.getType();
        if (type == Material.BARREL) {
            player.playSound(location, Sound.BLOCK_BARREL_CLOSE, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        } else if (type == Material.CHEST) {
            player.playSound(location, Sound.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        } else if (type == Material.ENDER_CHEST) {
            player.playSound(location, Sound.BLOCK_ENDER_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        } else if (state instanceof ShulkerBox shulker) {
            player.playSound(location, Sound.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        }
    }

    public static long getSeed(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();
        return location.getWorld().getSeed() | ProtoChunk.posAsLong(x >> 4, z >> 4) | Vec3i.packByte(x, z);
    }

    public static BlockState getBlockState(Location location) {
        Scheduler scheduler = JustLootItPlugin.get().platform().scheduler();
        if (scheduler.isRegional() || !Bukkit.isPrimaryThread()) {
            return scheduler.syncRegional(location, () -> location.getWorld().getBlockState(location)).join();
        }
        return location.getWorld().getBlockState(location);
    }

}
