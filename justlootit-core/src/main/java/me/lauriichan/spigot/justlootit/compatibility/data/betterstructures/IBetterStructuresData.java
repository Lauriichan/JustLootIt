package me.lauriichan.spigot.justlootit.compatibility.data.betterstructures;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;

import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.compatibility.data.ICompatibilityData;
import me.lauriichan.spigot.justlootit.compatibility.provider.CompatDependency;
import me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures.IBetterStructuresProvider;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public interface IBetterStructuresData extends ICompatibilityData {
    
    String fileName();
    
    default boolean canFill(BlockState state, Location location) {
        IBetterStructuresProvider provider = CompatDependency.getActiveProvider(extension().id(), IBetterStructuresProvider.class);
        return provider != null && provider.access().hasLootForFile(fileName());
    }
    
    @Override
    default boolean fill(CompatibilityContainer container, PlayerAdapter player, BlockState state, Location location, Inventory inventory) {
        IBetterStructuresProvider provider = CompatDependency.getActiveProvider(extension().id(), IBetterStructuresProvider.class);
        return provider != null && provider.access().fillWithLootForFile(inventory, fileName());
    }
    
    @Override
    default void addInfoData(Consumer<Key> add) {
        add.accept(Key.of("Settings file", fileName()));
    }

}
