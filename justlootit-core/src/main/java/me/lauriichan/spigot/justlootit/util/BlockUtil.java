package me.lauriichan.spigot.justlootit.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.RegionAccessor;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Player;

public final class BlockUtil {

    private BlockUtil() {
        throw new UnsupportedOperationException();
    }

    public static Container findChestAround(RegionAccessor region, Location location, Type chestType, BlockFace chestFace) {
        return findChestAround(region, location.getBlockX(), location.getBlockY(), location.getBlockZ(), chestType, chestFace);
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
        if (location.getWorld() != null && !location.getWorld().getUID().equals(player.getWorld().getUID())) {
            return;
        }
        BlockState state = (location.getWorld() != null ? location.getWorld() : player.getWorld()).getBlockState(location);
        Material type = state.getType();
        if (type == Material.BARREL) {
            player.playSound(location, Sound.BLOCK_BARREL_OPEN, 0.5f, 1f);
            return;
        } else if (type == Material.CHEST) {
            player.playSound(location, Sound.BLOCK_CHEST_OPEN, 0.5f, 1f);
            return;
        } else if (type == Material.ENDER_CHEST) {
            player.playSound(location, Sound.BLOCK_ENDER_CHEST_OPEN, 0.5f, 1f);
            return;
        } else if (state instanceof ShulkerBox shulker) {
            player.playSound(location, Sound.BLOCK_SHULKER_BOX_OPEN, 0.5f, 1f);
            return;
        }
    }

    public static void sendBlockClose(Player player, Location location) {
        if (location.getWorld() != null && !location.getWorld().getUID().equals(player.getWorld().getUID())) {
            return;
        }
        BlockState state = (location.getWorld() != null ? location.getWorld() : player.getWorld()).getBlockState(location);
        Material type = state.getType();
        if (type == Material.BARREL) {
            player.playSound(location, Sound.BLOCK_BARREL_CLOSE, 0.5f, 1f);
            return;
        } else if (type == Material.CHEST) {
            player.playSound(location, Sound.BLOCK_CHEST_CLOSE, 0.5f, 1f);
            return;
        } else if (type == Material.ENDER_CHEST) {
            player.playSound(location, Sound.BLOCK_ENDER_CHEST_CLOSE, 0.5f, 1f);
            return;
        } else if (state instanceof ShulkerBox shulker) {
            player.playSound(location, Sound.BLOCK_SHULKER_BOX_CLOSE, 0.5f, 1f);
            return;
        }
    }

}
