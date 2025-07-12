package me.lauriichan.spigot.justlootit.compatibility.provider.iris;

import org.bukkit.World;

import me.lauriichan.spigot.justlootit.compatibility.data.iris.IIrisLootCache;
import me.lauriichan.spigot.justlootit.compatibility.data.iris.IIrisTableKey;

public interface IIrisAccess {
    
    boolean isIrisWorld(World world);

    IIrisLootCache loadLootTables(World world, IIrisTableKey[] keys);
    
}
