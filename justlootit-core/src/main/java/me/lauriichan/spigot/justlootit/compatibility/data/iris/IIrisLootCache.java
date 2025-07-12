package me.lauriichan.spigot.justlootit.compatibility.data.iris;

import org.bukkit.World;
import org.bukkit.inventory.Inventory;

public interface IIrisLootCache {
    
    boolean isNotEmpty();
    
    void fill(Inventory inventory, long seed, World world, int x, int y, int z);
    
}
