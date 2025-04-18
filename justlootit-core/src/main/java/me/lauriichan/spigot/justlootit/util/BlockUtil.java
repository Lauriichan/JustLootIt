package me.lauriichan.spigot.justlootit.util;

import org.bukkit.Bukkit;
import org.bukkit.GameEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.RegionAccessor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;

import me.lauriichan.spigot.justlootit.JustLootItAccess;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.nms.LevelAdapter;
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
    
    public static Container getNearbyChest(RegionAccessor region, Container container) {
        BlockData data = container.getBlockData();
        if (!(data instanceof Chest chest) || chest.getType() == Type.SINGLE) {
            return null;
        }
        return BlockUtil.findChestAround(region, container.getX(), container.getY(), container.getZ(),
            chest.getType(), chest.getFacing());
    }
    
    public static Container getNearbyChest(Container container) {
        BlockData data = container.getBlockData();
        if (!(data instanceof Chest chest) || chest.getType() == Type.SINGLE) {
            return null;
        }
        return BlockUtil.findChestAround(container.getWorld(), container.getX(), container.getY(), container.getZ(),
            chest.getType(), chest.getFacing());
    }
    
    public static void setContainerOffsetToNearbyChest(RegionAccessor region, Container container) {
        BlockData data = container.getBlockData();
        if (!(data instanceof Chest chest) || chest.getType() == Type.SINGLE) {
            return;
        }
        Container otherContainer = BlockUtil.findChestAround(region, container.getX(), container.getY(), container.getZ(),
            chest.getType(), chest.getFacing());
        if (otherContainer == null) {
            return;
        }
        JustLootItAccess.setOffset(otherContainer.getPersistentDataContainer(), calculateOffset(otherContainer.getLocation(), container.getLocation()));
        JustLootItAccess.setOffset(container.getPersistentDataContainer(), calculateOffset(container.getLocation(), otherContainer.getLocation()));
        otherContainer.update(false, false);
        // The container this is executed on needs to be updated afterwards.
    }
    
    public static void setContainerOffsetToNearbyChest(Container container) {
        setContainerOffsetToNearbyChest(container.getWorld(), container);
    }
    
    public static void setContainerOffset(Container container, Container otherContainer, boolean update) {
        JustLootItAccess.setOffset(otherContainer.getPersistentDataContainer(), calculateOffset(otherContainer.getLocation(), container.getLocation()));
        JustLootItAccess.setOffset(container.getPersistentDataContainer(), calculateOffset(container.getLocation(), otherContainer.getLocation()));
        if (update) {
            otherContainer.update(false, false);
            container.update(false, false);
        }
    }

    public static Container getContainerByOffset(Container otherContainer) {
        PersistentDataContainer otherDataContainer = otherContainer.getPersistentDataContainer();
        Vec3i offset;
        boolean legacy = false;
        if (JustLootItAccess.hasOffset(otherDataContainer)) {
            offset = JustLootItAccess.getOffset(otherDataContainer);
        } else {
            if (JustLootItAccess.hasOffsetV1(otherDataContainer)) {
                offset = JustLootItAccess.getOffsetV1(otherDataContainer);
                JustLootItAccess.removeOffsetV1(otherDataContainer);
                JustLootItAccess.setOffset(otherDataContainer, offset);
                legacy = true;
            } else if (JustLootItAccess.hasLegacyOffset(otherDataContainer)) {
                offset = JustLootItAccess.getLegacyOffset(otherDataContainer);
                JustLootItAccess.removeLegacyOffset(otherDataContainer);
                JustLootItAccess.setOffset(otherDataContainer, offset);
                legacy = true;
            } else {
                return null;
            }
        }
        if (!(otherContainer.getBlockData() instanceof Chest chest) || chest.getType() == Chest.Type.SINGLE){
            JustLootItAccess.removeOffset(otherDataContainer);
            otherContainer.update(false, false);
            return null;
        }
        Location blockLocation = offset.addOn(otherContainer.getLocation());
        BlockState state = blockLocation.getWorld().getBlockState(blockLocation);
        if (state instanceof Container container) {
            if (legacy) {
                PersistentDataContainer dataContainer = container.getPersistentDataContainer();
                if (JustLootItAccess.hasLegacyOffset(dataContainer)) {
                    offset = JustLootItAccess.getLegacyOffset(dataContainer);
                    JustLootItAccess.removeLegacyOffset(dataContainer);
                    JustLootItAccess.setOffset(dataContainer, offset);
                    container.update(false, false);
                } else if(!JustLootItAccess.hasOffsetV1(dataContainer)) {
                    offset = JustLootItAccess.getOffsetV1(dataContainer);
                    JustLootItAccess.removeOffsetV1(dataContainer);
                    JustLootItAccess.setOffset(dataContainer, offset);
                    container.update(false, false);
                } else if(!JustLootItAccess.hasOffset(dataContainer)) {
                    JustLootItAccess.setOffset(dataContainer, calculateOffset(blockLocation, otherContainer.getLocation()));
                    container.update(false, false);
                }
                otherContainer.update(false, false);
            }
            return container;
        }
        JustLootItAccess.removeOffset(otherDataContainer);
        otherContainer.update(false, false);
        return null;
    }
    
    public static Vec3i calculateOffset(Location origin, Location other) {
        return new Vec3i(other).subtractOf(origin);
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

    public static void sendBlockOpen(LevelAdapter level, Player player, Location location) {
        BlockState state = player.getWorld().getBlockState(location);
        Material type = state.getType();
        if (type == Material.BARREL) {
            level.triggerGameEvent(player, GameEvent.CONTAINER_OPEN, location);
            player.playSound(location, Sound.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        } else if (type == Material.CHEST) {
            level.triggerGameEvent(player, GameEvent.CONTAINER_OPEN, location);
            player.playSound(location, Sound.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        } else if (type == Material.ENDER_CHEST) {
            level.triggerGameEvent(player, GameEvent.CONTAINER_OPEN, location);
            player.playSound(location, Sound.BLOCK_ENDER_CHEST_OPEN, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        } else if (state instanceof ShulkerBox) {
            level.triggerGameEvent(player, GameEvent.CONTAINER_OPEN, location);
            player.playSound(location, Sound.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        }
    }

    public static void sendBlockClose(LevelAdapter level, Player player, Location location) {
        BlockState state = player.getWorld().getBlockState(location);
        Material type = state.getType();
        if (type == Material.BARREL) {
            level.triggerGameEvent(player, GameEvent.CONTAINER_CLOSE, location);
            player.playSound(location, Sound.BLOCK_BARREL_CLOSE, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        } else if (type == Material.CHEST) {
            level.triggerGameEvent(player, GameEvent.CONTAINER_CLOSE, location);
            player.playSound(location, Sound.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        } else if (type == Material.ENDER_CHEST) {
            level.triggerGameEvent(player, GameEvent.CONTAINER_CLOSE, location);
            player.playSound(location, Sound.BLOCK_ENDER_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        } else if (state instanceof ShulkerBox) {
            level.triggerGameEvent(player, GameEvent.CONTAINER_CLOSE, location);
            player.playSound(location, Sound.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 0.5f, 1f);
            return;
        }
    }

    public static long getSeed(Location location) {
        int x = location.getBlockX();
        int z = location.getBlockZ();
        return location.getWorld().getSeed() | ProtoChunk.posAsLong(x >> 4, z >> 4) | Vec3i.packUnsignedByte(x, z);
    }

    public static long getSeed(ProtoChunk chunk, Vec3i location) {
        return chunk.getWorld().getSeed() | chunk.getPosAsLong() | location.packUnsignedByte();
    }

    public static BlockState getBlockState(Location location) {
        Scheduler scheduler = JustLootItPlugin.get().platform().scheduler();
        if (scheduler.isRegional() || !Bukkit.isPrimaryThread()) {
            return scheduler.syncRegional(location, () -> location.getWorld().getBlockState(location)).join();
        }
        return location.getWorld().getBlockState(location);
    }

}
