package me.lauriichan.spigot.justlootit.compatibility.data.iris;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;

import me.lauriichan.laylib.localization.Key;
import me.lauriichan.spigot.justlootit.compatibility.data.ICompatibilityData;
import me.lauriichan.spigot.justlootit.compatibility.provider.CompatDependency;
import me.lauriichan.spigot.justlootit.compatibility.provider.iris.IIrisAccess;
import me.lauriichan.spigot.justlootit.compatibility.provider.iris.IIrisProvider;
import me.lauriichan.spigot.justlootit.data.CompatibilityContainer;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public interface IIrisData extends ICompatibilityData {

    long seed();

    IIrisTableKey[] keys();

    IIrisLootCache cache(IIrisAccess access, World world);

    @Override
    default boolean canFill(BlockState state, Location location) {
        IIrisProvider provider = CompatDependency.getActiveProvider(extension().id(), IIrisProvider.class);
        if (provider == null) {
            return false;
        }
        World world = state.getWorld();
        if (!provider.access().isIrisWorld(world)) {
            return false;
        }
        return cache(provider.access(), world).isNotEmpty();
    }

    @Override
    default boolean fill(CompatibilityContainer container, PlayerAdapter player, BlockState state, Location location, Inventory inventory) {
        IIrisProvider provider = CompatDependency.getActiveProvider(extension().id(), IIrisProvider.class);
        if (provider == null) {
            return false;
        }
        cache(provider.access(), state.getWorld()).fill(inventory, seed(), state.getWorld(), location.getBlockX(), location.getBlockY(),
            location.getBlockZ());
        return true;
    }

    @Override
    default void addInfoData(Consumer<Key> add) {
        add.accept(Key.of("Seed", seed()));
        for (IIrisTableKey key : keys()) {
            add.accept(Key.of("Loot Tables", key.identifier()));
        }
    }

}
