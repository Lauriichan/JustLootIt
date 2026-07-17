package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import org.bukkit.inventory.Inventory;

import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

public interface IBetterStructuresAccess {
    
    boolean hasLootForGeneratorFile(String fileName);
    
    boolean fillWithLootForGeneratorFile(Inventory inventory, String fileName);
    
    boolean hasLootForTreasureFile(String fileName);
    
    boolean fillWithLootForTreasureFile(Inventory inventory, String fileName);
    
    String migrateGeneratorFileToTreasureFile(String fileName);
    
    void provideLootTableKeys(CategorizedKeyMap keyMap);

}
