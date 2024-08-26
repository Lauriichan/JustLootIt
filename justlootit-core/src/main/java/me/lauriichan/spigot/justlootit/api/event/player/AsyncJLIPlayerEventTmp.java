package me.lauriichan.spigot.justlootit.api.event.player;

import me.lauriichan.maven.justlootit.annotation.Event;
import me.lauriichan.maven.justlootit.annotation.EventField;
import me.lauriichan.spigot.justlootit.api.event.AsyncJLIEvent;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;

@Event(isAbstract = true)
public abstract class AsyncJLIPlayerEventTmp extends AsyncJLIEvent {

    @EventField
    private PlayerAdapter player;

}
