package me.lauriichan.spigot.justlootit.api.event.player;

import org.bukkit.loot.LootTable;

import me.lauriichan.maven.justlootit.annotation.Event;
import me.lauriichan.maven.justlootit.annotation.EventField;

@Event
public abstract class AsyncJLIPlayerVanillaLootProvidedEventTmp extends AsyncJLIPlayerLootProvidedEventTmp {
    
    @EventField
    private LootTable lootTable;
    
    @EventField
    private long seed;

}
