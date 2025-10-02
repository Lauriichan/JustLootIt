package me.lauriichan.spigot.justlootit.api.event.player;

import org.bukkit.loot.LootTable;

import me.lauriichan.maven.justlootit.annotation.Event;
import me.lauriichan.maven.justlootit.annotation.EventField;

@Event(isIndependent = false)
public abstract class JLIPlayerVanillaLootProvidedEventTmp extends JLIPlayerLootProvidedEventTmp {
    
    @EventField
    private LootTable lootTable;
    
    @EventField
    private long seed;

}
