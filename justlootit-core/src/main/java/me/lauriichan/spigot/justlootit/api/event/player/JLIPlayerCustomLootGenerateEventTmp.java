package me.lauriichan.spigot.justlootit.api.event.player;

import me.lauriichan.maven.justlootit.annotation.Event;
import me.lauriichan.maven.justlootit.annotation.EventField;
import me.lauriichan.spigot.justlootit.config.data.CustomLootTable;

@Event
public abstract class JLIPlayerCustomLootGenerateEventTmp extends JLIPlayerEventTmp {
    
    @EventField(setter = false)
    private CustomLootTable lootTable;
    
    @EventField(setter = true)
    private long seed;

}
