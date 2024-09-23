package me.lauriichan.spigot.justlootit.data;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.api.event.player.AsyncJLIPlayerLootProvidedEvent;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public interface IInventoryContainer {
    
    static abstract interface IResult {
        public static IResult empty() {
            return EmptyResult.EMPTY;
        }
    }
    
    static final class EmptyResult implements IResult {
        private static final EmptyResult EMPTY = new EmptyResult();
        private EmptyResult() {
            if (EMPTY != null) {
                throw new UnsupportedOperationException();
            }
        }
    }

    default IResult fill(PlayerAdapter player, InventoryHolder holder, Location location, Inventory inventory) {
        return IResult.empty();
    }
    
    default void fillNoResult(PlayerAdapter player, InventoryHolder holder, Location location, Inventory inventory) {
        fill(player, holder, location, inventory);
    }

    default void awaitProvidedEvent(PlayerAdapter player, IGuiInventory inventory, InventoryHolder entryHolder, Location entryLocation, IResult result) {
        new AsyncJLIPlayerLootProvidedEvent((JustLootItPlugin) player.versionHandler().plugin(), player, inventory, entryHolder, entryLocation).call().join();
    }

}
