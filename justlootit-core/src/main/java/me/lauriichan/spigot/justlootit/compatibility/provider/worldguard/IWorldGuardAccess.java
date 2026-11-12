package me.lauriichan.spigot.justlootit.compatibility.provider.worldguard;

import me.lauriichan.spigot.justlootit.compatibility.support.IAccessSupport;
import me.lauriichan.spigot.justlootit.compatibility.support.IDestructionSupport;

public interface IWorldGuardAccess extends IAccessSupport, IDestructionSupport {
    
    void init();
    
}
