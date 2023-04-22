package me.lauriichan.spigot.justlootit.util;

import org.bukkit.Location;
import org.bukkit.RegionAccessor;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Chest.Type;

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

}
