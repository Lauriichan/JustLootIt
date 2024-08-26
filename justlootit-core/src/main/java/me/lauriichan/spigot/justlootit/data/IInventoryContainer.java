package me.lauriichan.spigot.justlootit.data;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import me.lauriichan.minecraft.pluginbase.inventory.IGuiInventory;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.api.event.player.AsyncJLIPlayerLootProvidedEvent;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public interface IInventoryContainer {

    void fill(PlayerAdapter player, InventoryHolder holder, Location location, Inventory inventory);

    default void awaitProvidedEvent(PlayerAdapter player, IGuiInventory inventory, InventoryHolder entryHolder, Location entryLocation) {
        new AsyncJLIPlayerLootProvidedEvent((JustLootItPlugin) player.versionHandler().plugin(), player, inventory, entryHolder, entryLocation).call().join();
    }

}
