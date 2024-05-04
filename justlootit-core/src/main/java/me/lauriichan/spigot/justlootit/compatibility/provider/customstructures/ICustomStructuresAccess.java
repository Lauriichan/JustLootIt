package me.lauriichan.spigot.justlootit.compatibility.provider.customstructures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

public interface ICustomStructuresAccess {
    
    boolean hasLootTable(String name, Material type);
    
    boolean fillWithLootTable(Inventory inventory, Material type, Location location, String name, long seed);

}
