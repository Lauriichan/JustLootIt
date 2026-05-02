package me.lauriichan.spigot.justlootit.compatibility.provider.customstructures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

public interface ICustomStructuresAccess {
    
    boolean hasLootTable(String name);
    
    boolean fillWithLootTable(Inventory inventory, Location location, String name, long seed);
    
    boolean hasStructureLootTable(String name, Material type);
    
    boolean fillWithStructureLootTable(Inventory inventory, Material type, Location location, String name, long seed);

    void provideLootTableKeys(CategorizedKeyMap keyMap);

}
