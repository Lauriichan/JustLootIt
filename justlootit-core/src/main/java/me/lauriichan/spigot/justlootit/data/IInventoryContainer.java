package me.lauriichan.spigot.justlootit.data;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

public interface IInventoryContainer {

    void fill(PlayerAdapter player, InventoryHolder holder, Location location, Inventory inventory);

}
