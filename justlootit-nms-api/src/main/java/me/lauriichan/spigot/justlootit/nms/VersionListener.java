package me.lauriichan.spigot.justlootit.nms;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

final class VersionListener implements Listener {

    private final VersionHandler handler;

    public VersionListener(final VersionHandler handler) {
        this.handler = handler;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent event) {
        handler.join(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(final PlayerQuitEvent event) {
        handler.quit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldLoad(final WorldLoadEvent event) {
        handler.load(event.getWorld());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldUnload(final WorldUnloadEvent event) {
        handler.unload(event.getWorld());
    }

}
