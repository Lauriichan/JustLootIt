package me.lauriichan.spigot.justlootit.api.event;

import me.lauriichan.maven.justlootit.annotation.Event;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;

@Event
public abstract class AsyncJLIEvent extends JLIEvent {

    /**
     * This constructor only exists for development to be easier as the constructors
     * are automatically generated on compilation.
     */
    public AsyncJLIEvent() {
        super();
    }

    public AsyncJLIEvent(final JustLootItPlugin plugin) {
        super(true, plugin);
    }

}
