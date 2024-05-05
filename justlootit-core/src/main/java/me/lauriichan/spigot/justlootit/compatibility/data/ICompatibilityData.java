package me.lauriichan.spigot.justlootit.compatibility.data;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;

import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public interface ICompatibilityData {
    
    CompatibilityDataExtension<?> extension();
    
    int version();
    
    default boolean canFill(BlockState state, Location location) {
        return false;
    }
    
    default boolean canFill(Entity entity, Location location) {
        return false;
    }

    default boolean fill(PlayerAdapter player, BlockState state, Location location, Inventory inventory) {
        return false;
    }

    default boolean fill(PlayerAdapter player, Entity entity, Location location, Inventory inventory) {
        return false;
    }
    
    default void addInfoData(Consumer<Key> add) {}
    
}
