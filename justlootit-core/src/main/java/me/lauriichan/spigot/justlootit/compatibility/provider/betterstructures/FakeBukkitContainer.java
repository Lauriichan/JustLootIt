package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("deprecation")
public class FakeBukkitContainer implements Container {

    private final Inventory inventory;
    
    public FakeBukkitContainer(final Inventory inventory) {
        this.inventory = inventory;
    }
    
    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return null;
    }

    @Override
    public Block getBlock() {
        return null;
    }

    @Override
    public MaterialData getData() {
        return null;
    }

    @Override
    public BlockData getBlockData() {
        return null;
    }

    @Override
    public BlockState copy() {
        return null;
    }

    @Override
    public Material getType() {
        return null;
    }

    @Override
    public byte getLightLevel() {
        return 0;
    }

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getZ() {
        return 0;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public Location getLocation(Location loc) {
        return null;
    }

    @Override
    public Chunk getChunk() {
        return null;
    }

    @Override
    public void setData(MaterialData data) {}

    @Override
    public void setBlockData(BlockData data) {}

    @Override
    public void setType(Material type) {}

    @Override
    public boolean update() {
        return false;
    }

    @Override
    public boolean update(boolean force) {
        return false;
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        return false;
    }

    @Override
    public byte getRawData() {
        return 0;
    }

    @Override
    public void setRawData(byte data) {}

    @Override
    public boolean isPlaced() {
        return false;
    }

    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {}

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return null;
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return false;
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {}

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public String getLock() {
        return null;
    }

    @Override
    public void setLock(String key) {}

    @Override
    public String getCustomName() {
        return null;
    }

    @Override
    public void setCustomName(String name) {}

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public Inventory getSnapshotInventory() {
        return inventory;
    }
    
}