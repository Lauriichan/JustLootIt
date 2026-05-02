package me.lauriichan.spigot.justlootit.compatibility.data.customstructures;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;

import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.provider.CompatDependency;
import me.lauriichan.spigot.justlootit.compatibility.provider.customstructures.ICustomStructuresProvider;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public record CustomStructuresDataV2(CompatibilityDataExtension<?> extension, NamespacedKey dataId, String tableName, long seed) implements ICustomStructuresData {

    @Override
    public int version() {
        return 1;
    }

    @Override
    public boolean canFill(BlockState state, Location location) {
        ICustomStructuresProvider provider = CompatDependency.getActiveProvider(extension().id(), ICustomStructuresProvider.class);
        return provider != null && provider.access().hasLootTable(tableName);
    }
    
    @Override
    public boolean fill(CompatibilityContainer container, PlayerAdapter player, BlockState state, Location location, Inventory inventory) {
        ICustomStructuresProvider provider = CompatDependency.getActiveProvider(extension().id(), ICustomStructuresProvider.class);
        if (provider == null) {
            return false;
        }
        return provider.access().fillWithLootTable(inventory, location, tableName, container.generateSeed(location.getWorld(), player, seed()));
    }

    @Override
    public String refreshContainerId() {
        return tableName;
    }
    
    @Override
    public void addInfoData(Consumer<Key> add) {
        add.accept(Key.of("Loot Table", tableName));
        add.accept(Key.of("Seed", seed));
    }

}
