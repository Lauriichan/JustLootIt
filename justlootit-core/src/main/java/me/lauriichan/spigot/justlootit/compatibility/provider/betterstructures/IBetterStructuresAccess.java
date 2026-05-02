package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import org.bukkit.inventory.Inventory;

import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

public interface IBetterStructuresAccess {
    
    boolean hasLootForFile(String fileName);
    
    boolean fillWithLootForFile(Inventory inventory, String fileName);
    
    void provideLootTableKeys(CategorizedKeyMap keyMap);

}
