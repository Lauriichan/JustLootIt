package me.lauriichan.spigot.justlootit.nms.capability.impl;

import java.lang.ref.WeakReference;

import org.bukkit.entity.Player;

import me.lauriichan.laylib.command.Actor;
import me.lauriichan.spigot.justlootit.nms.IServiceProvider;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.capability.ICapability;

public final class ActorCapability implements ICapability {

    private final PlayerAdapter adapter;
    private final IServiceProvider serviceProvider;
    
    private WeakReference<Actor<Player>> actor;
    
    public ActorCapability(PlayerAdapter adapter, IServiceProvider serviceProvider) {
        this.adapter = adapter;
        this.serviceProvider = serviceProvider;
    }
    
    public Actor<Player> actor() {
        if (this.actor == null) {
            Player player = adapter.asBukkit();
            if (player == null) {
                return null;
            }
            Actor<Player> actor = serviceProvider.playerActor(player);
            this.actor = new WeakReference<>(actor);
            return actor;
        }
        Actor<Player> actor = this.actor.get();
        if (actor == null) {
            this.actor = null;
            return actor();
        }
        return actor;
    }
    
}
