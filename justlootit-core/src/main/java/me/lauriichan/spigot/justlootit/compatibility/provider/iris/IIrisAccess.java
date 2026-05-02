package me.lauriichan.spigot.justlootit.compatibility.provider.iris;

import org.bukkit.World;

import me.lauriichan.spigot.justlootit.compatibility.data.iris.IIrisLootCache;
import me.lauriichan.spigot.justlootit.compatibility.data.iris.IIrisTableKey;
import me.lauriichan.spigot.justlootit.util.CategorizedKeyMap;

public interface IIrisAccess {
    
    boolean isIrisWorld(World world);

    IIrisLootCache loadLootTables(World world, IIrisTableKey[] keys);

    boolean provideLootTableKeys(World world, CategorizedKeyMap keyMap);
    
}
