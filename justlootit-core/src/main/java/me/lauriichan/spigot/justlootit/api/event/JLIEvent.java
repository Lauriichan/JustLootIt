package me.lauriichan.spigot.justlootit.api.event;

import java.util.Objects;

import org.bukkit.Server;

import me.lauriichan.maven.justlootit.annotation.Event;
import me.lauriichan.maven.justlootit.annotation.EventField;
import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.VersionHelper;
import me.lauriichan.spigot.justlootit.platform.scheduler.Scheduler;
import me.lauriichan.spigot.justlootit.platform.scheduler.SimpleTask;
import me.lauriichan.spigot.justlootit.platform.scheduler.Task;

@Event
public abstract class JLIEvent extends org.bukkit.event.Event {

    @EventField
    private final JustLootItPlugin plugin;

    /**
     * This constructor only exists for development to be easier as the constructors
     * are automatically generated on compilation.
     */
    public JLIEvent() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public JLIEvent(final JustLootItPlugin plugin) {
        super(false);
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    JLIEvent(final boolean async, final JustLootItPlugin plugin) {
        super(async);
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    public final JustLootItPlugin plugin() {
        return plugin;
    }
    
    public final Scheduler scheduler() {
        return plugin.platform().scheduler();
    }

    public final VersionHandler versionHandler() {
        return plugin.versionHandler();
    }

    public final VersionHelper versionHelper() {
        return plugin.versionHelper();
    }

    public final Task<Void> call() {
        Server server = plugin.getServer();
        if (isAsynchronous() && server.isPrimaryThread()) {
            return scheduler().async(() -> server.getPluginManager().callEvent(this));
        }
        server.getPluginManager().callEvent(this);
        return SimpleTask.newCompleted(plugin.logger());
    }

}
