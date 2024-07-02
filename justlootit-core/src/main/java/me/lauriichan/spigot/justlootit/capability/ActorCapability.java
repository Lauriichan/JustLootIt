package me.lauriichan.spigot.justlootit.capability;

import java.lang.ref.WeakReference;

import org.bukkit.entity.Player;

import me.lauriichan.spigot.justlootit.JustLootItPlugin;
import me.lauriichan.spigot.justlootit.command.impl.LootItActor;
import me.lauriichan.spigot.justlootit.nms.PlayerAdapter;
import me.lauriichan.spigot.justlootit.nms.VersionHandler;
import me.lauriichan.spigot.justlootit.nms.capability.ICapability;

public final class ActorCapability implements ICapability {

    public static LootItActor<Player> actor(JustLootItPlugin plugin, Player player) {
        return actor(plugin.versionHandler(), player);
    }

    public static LootItActor<Player> actor(VersionHandler handler, Player player) {
        return handler.getPlayer(player).getCapability(ActorCapability.class).get().actor();
    }

    public static LootItActor<Player> actor(PlayerAdapter adapter) {
        return adapter.getCapability(ActorCapability.class).get().actor();
    }

    private final PlayerAdapter adapter;
    private final JustLootItPlugin plugin;

    private WeakReference<LootItActor<Player>> actor;

    public ActorCapability(PlayerAdapter adapter) {
        this.adapter = adapter;
        this.plugin = (JustLootItPlugin) adapter.versionHandler().plugin();
    }

    public LootItActor<Player> actor() {
        if (this.actor == null) {
            Player player = adapter.asBukkit();
            if (player == null) {
                return null;
            }
            LootItActor<Player> actor = plugin.actor(player);
            this.actor = new WeakReference<>(actor);
            return actor;
        }
        LootItActor<Player> actor = this.actor.get();
        if (actor == null) {
            this.actor = null;
            return actor();
        }
        return actor;
    }

}
