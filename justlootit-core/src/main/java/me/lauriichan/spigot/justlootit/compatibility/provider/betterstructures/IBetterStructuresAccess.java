package me.lauriichan.spigot.justlootit.compatibility.provider.betterstructures;

import org.bukkit.inventory.Inventory;

public interface IBetterStructuresAccess {
    
    boolean hasLootForFile(String fileName);
    
    boolean fillWithLootForFile(Inventory inventory, String fileName);

}
