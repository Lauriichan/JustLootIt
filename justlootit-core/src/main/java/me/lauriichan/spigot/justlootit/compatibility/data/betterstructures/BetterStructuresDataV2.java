package me.lauriichan.spigot.justlootit.compatibility.data.betterstructures;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;

import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.compatibility.data.CompatibilityDataExtension;
import me.lauriichan.spigot.justlootit.compatibility.provider.CompatDependency;
import me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures.IBetterStructuresProvider;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public record BetterStructuresDataV2(CompatibilityDataExtension<?> extension, NamespacedKey dataId, String fileName)
    implements IBetterStructuresData {

    @Override
    public boolean canFill(BlockState state, Location location) {
        IBetterStructuresProvider provider = CompatDependency.getActiveProvider(extension().id(), IBetterStructuresProvider.class);
        return provider != null && provider.access().hasLootForTreasureFile(fileName());
    }
    
    @Override
    public boolean fill(CompatibilityContainer container, PlayerAdapter player, BlockState state, Location location, Inventory inventory) {
        IBetterStructuresProvider provider = CompatDependency.getActiveProvider(extension().id(), IBetterStructuresProvider.class);
        return provider != null && provider.access().fillWithLootForTreasureFile(inventory, fileName());
    }
    
    @Override
    public void addInfoData(Consumer<Key> add) {
        add.accept(Key.of("Treasure file", fileName()));
    }

    @Override
    public int version() {
        return 1;
    }

}
