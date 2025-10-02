package me.lauriichan.spigot.justlootit.api.event.player;

import org.bukkit.loot.LootTable;

import me.lauriichan.maven.justlootit.annotation.Event;
import me.lauriichan.maven.justlootit.annotation.EventField;

@Event
public abstract class JLIPlayerVanillaLootGenerateEventTmp extends JLIPlayerEventTmp {
    
    @EventField(setter = true)
    private LootTable lootTable;
    
    @EventField(setter = true)
    private long seed;

}
