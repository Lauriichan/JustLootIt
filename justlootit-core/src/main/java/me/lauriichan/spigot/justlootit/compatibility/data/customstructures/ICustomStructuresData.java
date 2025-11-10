package me.lauriichan.spigot.justlootit.compatibility.data.customstructures;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;

import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.compatibility.data.ICompatibilityData;
import me.lauriichan.spigot.justlootit.compatibility.provider.CompatDependency;
import me.lauriichan.spigot.justlootit.compatibility.provider.customstructures.ICustomStructuresProvider;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public interface ICustomStructuresData extends ICompatibilityData {
    
    String structureName();
    
    long seed();
    
    default boolean canFill(BlockState state, Location location) {
        ICustomStructuresProvider provider = CompatDependency.getActiveProvider(extension().id(), ICustomStructuresProvider.class);
        return provider != null && provider.access().hasLootTable(structureName(), state.getType());
    }
    
    @Override
    default boolean fill(CompatibilityContainer container, PlayerAdapter player, BlockState state, Location location, Inventory inventory) {
        ICustomStructuresProvider provider = CompatDependency.getActiveProvider(extension().id(), ICustomStructuresProvider.class);
        if (provider == null) {
            return false;
        }
        return provider.access().fillWithLootTable(inventory, state.getType(), location, structureName(), container.generateSeed(location.getWorld(), player, seed()));
    }
    
    @Override
    default void addInfoData(Consumer<Key> add) {
        add.accept(Key.of("Structure", structureName()));
        add.accept(Key.of("Seed", seed()));
    }
    
    @Override
    default String refreshContainerId() {
        return structureName();
    }

}
